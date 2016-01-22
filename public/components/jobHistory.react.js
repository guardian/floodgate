import React from 'react';
import { Table } from 'react-bootstrap';


export default class JobHistory extends React.Component {

    constructor(props) {
        super(props);
    }

    render () {

        var jobHistoryNodes = this.props.data.map(function(jobHistory) {
            return (
                <tr key={jobHistory.id}>
                    <td>{jobHistory.id}</td>
                    <td>{jobHistory.status}</td>
                    <td>{jobHistory.startTime}</td>
                    <td>{jobHistory.finishTime}</td>
                </tr>
            );
        });


        return (
            <div id="job-history">
                <Table striped hover>
                    <thead>
                    <tr>
                        <th>ID</th>
                        <th>Status</th>
                        <th>Start Time</th>
                        <th>Finish Time</th>
                    </tr>
                    </thead>
                    <tbody>
                        {jobHistoryNodes}
                    </tbody>
                </Table>
            </div>
        );
    }
}
