import type {GuStackProps} from "@guardian/cdk/lib/constructs/core";
import {GuParameter, GuStack} from "@guardian/cdk/lib/constructs/core";
import type {App} from "aws-cdk-lib";
import {aws_ssm, Stack} from "aws-cdk-lib";
import {GuEc2App} from "@guardian/cdk";
import {AccessScope} from "@guardian/cdk/lib/constants";
import {InstanceClass, InstanceSize, InstanceType, Peer, Port, UserData} from "aws-cdk-lib/aws-ec2";
import fs from "fs";
import {GuSecurityGroup} from "@guardian/cdk/lib/constructs/ec2";
import {GuPolicy} from "@guardian/cdk/lib/constructs/iam";
import {Effect, PolicyStatement} from "aws-cdk-lib/aws-iam";
import {Datastore} from "./datastore";

export class Floodgate extends GuStack {
  constructor(scope: App, id: string, props: GuStackProps) {
    super(scope, id, props);

    const app = "content-api-floodgate";

    const datastore = new Datastore(this, "Datastore");

    const dnsZone = aws_ssm.StringParameter.valueForStringParameter(this, `/account/services/capi.gutools/${this.stage}/hostedzoneid`);

    const prometheusRemoteWriteUrl = new GuParameter(this, "PromRemoteWrite", {
      description: "SSM path pointing to the parameter which gives an Amazon Managed Prometheus endpoint to push metrics to",
      fromSSM: true,
      type: "String",
      default: "/account/content-api-common/metrics/prometheus_remote_write_url"
    });

    const userDataRaw = fs.readFileSync("instance-startup.sh").toString('utf-8');
    const userDataProcessed = userDataRaw
        .replace(/\$\{Stage}/g, this.stage)
        .replace(/\$\{Stack}/g, this.stack)
        .replace(/\$\{AWS::Region}/g, Stack.of(this).region)
        .replace(/\$\{PrometheusRemoteWriteUrl}/g, prometheusRemoteWriteUrl.valueAsString)
        .replace(/\$\{BuiltVersion}/g, "1.0");

    const userData = UserData.custom(userDataProcessed)

    const ec2App = new GuEc2App(this, {
      access: {
        scope: AccessScope.PUBLIC,
      },
      app,
      applicationLogging: {
        enabled: true,
        systemdUnitName: "content-api-floodgate"
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
                      datastore.jobHistoryTable.tableArn,
                      datastore.contentSourceTable.tableArn,
                      datastore.runningJobTable.tableArn,
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
                  new PolicyStatement({
                    effect: Effect.ALLOW,
                    actions: [
                        "aps:RemoteWrite"
                    ],
                    resources: ["*"]
                  })
              ]
            })
        ]
      },
      applicationPort: 9000,
      certificateProps: {
        // TODO push this to props if/when we add a CODE stage
        domainName: "floodgate.capi.gutools.co.uk",
        hostedZoneId: dnsZone,
      },
      instanceType: InstanceType.of(InstanceClass.T4G, InstanceSize.SMALL),
      monitoringConfiguration: {
        snsTopicName: `floogate-monitoring-${this.stage}`,
        http5xxAlarm: {
          tolerated5xxPercentage: 0,
          numberOfMinutesAboveThresholdBeforeAlarm: 2,
        },
        unhealthyInstancesAlarm: true,
      },
      scaling: {
        minimumInstances: 1,
        maximumInstances: 2,
      },
      userData: userData,
      instanceMetricGranularity: "1Minute",
    });

    ec2App.autoScalingGroup.connections.addSecurityGroup(new GuSecurityGroup(this, "InstanceOutboundSG", {
      app,
      allowAllOutbound: false,
      allowAllIpv6Outbound: false,
      egresses: [
        {
          range: Peer.ipv4("10.248.0.0/16"),
          port: Port.tcp(8080),
          description: "Outgoing to port 8080 on internal infrastructure"
        }
      ],
      vpc: ec2App.vpc,
    }))

    /*
    These parameters are added to the stack via GuEc2App.
    By default, the VPC name used is "primary".
    Customise it to ensure the correct VPC is used for this app.
     */
    const maybeVpcId = this.parameters["VpcId"];
    const maybeVpcPublicSubnets = this.parameters[`${app}PublicSubnets`];
    const maybeVpcPrivateSubnets = this.parameters[`${app}PrivateSubnets`];

    if (!maybeVpcId || !maybeVpcPublicSubnets || !maybeVpcPrivateSubnets) {
      throw new Error("VPC parameters not found. Unable to synth valid CloudFormation template.");
    }

    const vpcName = `vpc-content-platforms-${this.stage}`;

    maybeVpcId.default = `/account/vpc/${vpcName}/id`;
    maybeVpcPublicSubnets.default = `/account/vpc/${vpcName}/subnets/public`;

    maybeVpcPrivateSubnets.default = `/account/vpc/${vpcName}/subnets/private`;
  }
}
