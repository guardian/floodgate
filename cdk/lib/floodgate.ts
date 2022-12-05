import type {GuStackProps} from "@guardian/cdk/lib/constructs/core";
import {GuParameter, GuStack} from "@guardian/cdk/lib/constructs/core";
import type {App} from "aws-cdk-lib";
import {aws_ssm, Stack} from "aws-cdk-lib";
import {GuEc2App} from "@guardian/cdk";
import {AccessScope} from "@guardian/cdk/lib/constants";
import {InstanceClass, InstanceSize, InstanceType, Peer, Vpc} from "aws-cdk-lib/aws-ec2";
import fs from "fs";
import {GuVpc} from "@guardian/cdk/lib/constructs/ec2";
import {GuPolicy} from "@guardian/cdk/lib/constructs/iam";
import {Effect, PolicyStatement} from "aws-cdk-lib/aws-iam";

export class Floodgate extends GuStack {
  constructor(scope: App, id: string, props: GuStackProps) {
    super(scope, id, props);

    const vpcId = aws_ssm.StringParameter.valueForStringParameter(this, this.getVpcIdPath());
    const vpc = Vpc.fromVpcAttributes(this, "vpc", {
      vpcId: vpcId,
      availabilityZones: ["eu-west-1a","eu-west-1b" ,"eu-west-1c"]
    });

    const subnetsList = new GuParameter(this, "subnets", {
      description: "Subnets to deploy into",
      default: this.getDeploymentSubnetsPath(),
      fromSSM: true,
      type: "List<String>"
    });
    const deploymentSubnets = GuVpc.subnets(this, subnetsList.valueAsList);

    const dnsZone = aws_ssm.StringParameter.valueForStringParameter(this, `/account/services/capi.gutools/${this.stage}/hostedzoneid`);

    const userDataRaw = fs.readFileSync("instance-startup.sh").toString('utf-8');
    const userData = userDataRaw
        .replace(/\$\{Stage}/g, this.stage)
        .replace(/\$\{Stack}/g, this.stack)
        .replace(/\$\{AWS::Region}/g, Stack.of(this).region)
        .replace(/\$\{BuiltVersion}/g, "0.1.0-SNAPSHOT");

    new GuEc2App(this, {
      access: {
        scope: AccessScope.RESTRICTED,
        cidrRanges: [Peer.ipv4("77.91.248.0/21")],
      },
      app: "floodgate",
      applicationLogging: {
        enabled: true,
      },
      roleConfiguration: {
        additionalPolicies: [
            new GuPolicy(this, "FloodgatePolicy", {
              statements: [
                new PolicyStatement({
                  effect: Effect.ALLOW,
                  actions: ["s3:GetObject"],
                  //we should already have access to content-api-dist "for free" because that's the default distribution bucket
                  resources: [
                      "arn:aws:s3:::content-api-config/*"
                  ]
                }),
                new PolicyStatement({
                  effect: Effect.ALLOW,
                  actions: ["dynamodb:*"],
                  resources: [
                      `arn:aws:dynamodb:*:${this.account}:table/floodgate-*-${this.stage}`
                  ]
                }),
                  new PolicyStatement({
                    effect: Effect.ALLOW,
                    actions: [
                        "ec2:DescribeInstances",
                        "ec2:DescribeTags",
                        "autoscaling:DescribeAutoScalingGroups",
                        "autoscaling:DescribeAutoScalingInstances"
                    ],
                    resources: ["*"]
                  }),
              ]
            })
        ]
      },
      applicationPort: 9000,
      certificateProps: {
        domainName: this.stage==="CODE" ? "floodgate.capi.code.dev-gutools.co.uk" : "floodgate,capi.gutools.co.uk",
        hostedZoneId: dnsZone,
      },
      instanceType: InstanceType.of(InstanceClass.T3, InstanceSize.SMALL),
      monitoringConfiguration: {
        snsTopicName: `floogate-monitoring-${this.stage}`,
        http5xxAlarm: {
          tolerated5xxPercentage: 0,
          numberOfMinutesAboveThresholdBeforeAlarm: 2,
        },
        unhealthyInstancesAlarm: true,
      },
      privateSubnets: deploymentSubnets,
      publicSubnets: deploymentSubnets,
      scaling: {
        minimumInstances: 1,
        maximumInstances: 2,
      },
      userData: userData,
      vpc,
    })
  }

  getAccountPath(elementName: string) {
    const basePath = "/account/vpc";
    if(this.stack.includes("preview")) {
      return this.stage=="CODE" ? `${basePath}/CODE-preview/${elementName}` : `${basePath}/PROD-preview/${elementName}`;
    } else {
      return this.stage=="CODE" ? `${basePath}/CODE-live/${elementName}` : `${basePath}/PROD-live/${elementName}`;
    }
  }

  getVpcIdPath() {
    return this.getAccountPath("id");
  }

  getDeploymentSubnetsPath() {
    return this.getAccountPath("subnets")
  }
}
