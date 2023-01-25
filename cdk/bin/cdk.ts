import "source-map-support/register";
import { App } from "aws-cdk-lib";
import { Floodgate } from "../lib/floodgate";

const app = new App();
new Floodgate(app, "Floodgate-PROD", { stack: "content-api-floodgate", stage: "PROD" });
new Floodgate(app, "Floodgate-CODE", { stack: "content-api-floodgate", stage: "CODE" });
