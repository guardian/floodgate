import type {GuStack} from "@guardian/cdk/lib/constructs/core";
import {Tags} from "aws-cdk-lib";
import type { ITable} from "aws-cdk-lib/aws-dynamodb";
import {AttributeType, BillingMode, Table, TableEncryption} from "aws-cdk-lib/aws-dynamodb";
import {Construct} from "constructs";

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

        // Enable automated backups for DynamoDB tables that we may need to restore (via https://github.com/guardian/aws-backup)
        [this.contentSourceTable, this.jobHistoryTable, ].map((table) => {
            Tags.of(table).add("devx-backup-enabled", "true");
        })

        // Explicitly opt-out of backups for runningJobTable as it's unlikely that we'd ever want to restore this
        Tags.of(this.runningJobTable).add("devx-backup-enabled", "false");

    }
}