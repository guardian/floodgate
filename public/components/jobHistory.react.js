import React from 'react';
import R from 'ramda';
import { Table } from 'react-bootstrap';


export default class JobHistory extends React.Component {

    constructor(props) {
        super(props);
    }

    render () {
        const jobHistoryNodes = this.props.data.map(jobHistory => {
            return (
                <tr key={jobHistory.startTime}>
                    <td>{jobHistory.status}</td>
                    <td>{ new Date(jobHistory.startTime).toUTCString() }</td>
                    <td>{ new Date(jobHistory.finishTime).toUTCString() }</td>
                </tr>
            );
        });

        return (
            <div id="job-history">
                {R.isEmpty(jobHistoryNodes) ?
                    <p>No reindex history. Have you initiated a reindex for this content via Floodgate before?</p>
                    :
                    <Table striped hover>
                        <thead>
                            <tr>
                                <th>Status</th>
                                <th>Start Time</th>
                                <th>Finish Time</th>
                            </tr>
                        </thead>
                        <tbody>
                            {jobHistoryNodes}
                        </tbody>
                    </Table>
                }
            </div>
        );
    }
}
