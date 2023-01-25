import { App } from "aws-cdk-lib";
import { Template } from "aws-cdk-lib/assertions";
import { Floodgate } from "./floodgate";

describe("The Floodgate stack", () => {
  it("matches the snapshot", () => {
    const app = new App();
    const stack = new Floodgate(app, "Floodgate", { stack: "content-api-floodgate", stage: "TEST" });
    const template = Template.fromStack(stack);
    expect(template.toJSON()).toMatchSnapshot();
  });
});
