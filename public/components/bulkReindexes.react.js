import React from 'react';
import { Panel, Button, Table } from 'react-bootstrap';
import RunningReindex from './runningReindex.react';


export default class BulkReindex extends React.Component {

    constructor(props) {
        super(props);

        this.cancelReindex = this.cancelReindex.bind(this);
        this.renderCancelButton = this.renderCancelButton.bind(this);
    }


    cancelReindex(reindex, isRunningJob) {
        this.props.onCancelReindex(reindex, isRunningJob);
    }

    renderCancelButton(reindex, isRunningJob) {
        if (isRunningJob && !reindex.settings.supportsCancelReindex) {
            return (null);
        } else {
            return (
                <Button bsStyle="danger" className="pull-right" type="button" onClick={this.cancelReindex.bind(this, reindex, isRunningJob)}>Cancel</Button>
            );
        }
    }

    render () {

        const completedJobsNodes = this.props.completedReindexes.map(completedJob => {
            return (
                <tr key={completedJob.startTime}>
                    <td>{completedJob.name}</td>
                    <td>{completedJob.env}</td>
                    <td>{completedJob.status}</td>
                    <td>{new Date(completedJob.startTime).toUTCString()}</td>
                    <td>{new Date(completedJob.finishTime).toUTCString()}</td>
                </tr>
            );
        });

        const runningJobsNodes = this.props.runningReindexes.map(runningJob => {
            return (
                <tr key={runningJob.startTime}>
                    <td>{runningJob.name}</td>
                    <td>{runningJob.env}</td>
                    <td>
                        <RunningReindex documentsIndexes={runningJob.documentsIndexed}
                                        documentsExpected={runningJob.documentsExpected}
                                        environment={runningJob.env}
                                        id={runningJob.id}
                                        startTime={runningJob.startTime}
                                        reindex={runningJob}
                                        isCancelSupported={false}
                                        onCancelReindex={this.cancelReindex.bind(this)}
                                        onReloadRunningReindex={this.props.onReloadRunningReindex}/>
                    </td>
                    <td>{new Date(runningJob.startTime).toUTCString()}</td>
                    <td>-</td>
                    <td>{ this.renderCancelButton(runningJob, true) }</td>
                </tr>
            );
        });

        const pendingJobsNodes = this.props.pendingReindexes.map(pendingJob => {
            return (
                <tr key={pendingJob.id + pendingJob.env}>
                    <td>{pendingJob.name}</td>
                    <td>{pendingJob.env}</td>
                    <td>Pending</td>
                    <td>-</td>
                    <td>-</td>
                    <td>{ this.renderCancelButton(pendingJob, false) }</td>
                </tr>
            );
        });

        return (
            <Panel header="Running Reindexes">
                <Table striped hover fill>
                    <thead>
                    <tr>
                        <th>App</th>
                        <th>Environment</th>
                        <th>Status</th>
                        <th>Start Time</th>
                        <th>Finish Time</th>
                        <th></th>
                    </tr>
                    </thead>
                    <tbody>
                        {completedJobsNodes}
                    </tbody>
                    <tbody>
                        {runningJobsNodes}
                    </tbody>
                    <tbody>
                        {pendingJobsNodes}
                    </tbody>
                </Table>
            </Panel>
        );
    }
}
