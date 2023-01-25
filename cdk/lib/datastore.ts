import {Construct} from "constructs";
import {GuStack} from "@guardian/cdk/lib/constructs/core";
import {AttributeType, BillingMode, ITable, Table, TableEncryption} from "aws-cdk-lib/aws-dynamodb";

export class Datastore extends Construct {
    contentSourceTable: ITable;
    jobHistoryTable: ITable;
    runningJobTable: ITable;

    constructor(scope:GuStack, id:string) {
        super(scope, id);

        this.contentSourceTable = new Table(scope, "ContentSource", {
            encryption: TableEncryption.AWS_MANAGED,
            partitionKey: {
                name: "id",
                type: AttributeType.STRING,
            },
            sortKey: {
                name: "environment",
                type: AttributeType.STRING,
            },
            billingMode: BillingMode.PAY_PER_REQUEST,
            tableName: `floodgate-content-source-${scope.stage}`,
        });

        this.jobHistoryTable = new Table(scope, "JobHistory", {
            encryption: TableEncryption.AWS_MANAGED,
            partitionKey: {
                name: "contentSourceId",
                type: AttributeType.STRING,
            },
            sortKey: {
                name: "startTime",
                type: AttributeType.STRING,
            },
            billingMode: BillingMode.PAY_PER_REQUEST,
            tableName: `floodgate-job-history-${scope.stage}`
        });

        this.runningJobTable = new Table(scope, "RunningJob", {
            encryption: TableEncryption.AWS_MANAGED,
            partitionKey: {
                name: "contentSourceId",
                type: AttributeType.STRING,
            },
            sortKey: {
                name: "contentSourceEnvironment",
                type: AttributeType.STRING,
            },
            billingMode: BillingMode.PAY_PER_REQUEST,
            tableName: `floodgate-running-job-${scope.stage}`
        });
    }
}