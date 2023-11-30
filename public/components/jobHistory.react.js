import React from 'react';
import * as R from 'ramda';
import { Table } from 'react-bootstrap';
import JobHistoryDateRange from './jobHistoryDateRange.react';


export default class JobHistory extends React.Component {

    constructor(props) {
        super(props);
    }

    render () {
        const jobHistoryNodes = this.props.data.map(jobHistory => {
            return (
                <tr key={jobHistory.startTime} className={jobHistory.status}>
                    <td>{jobHistory.status}</td>
                    <td><JobHistoryDateRange rangeFrom={jobHistory.rangeFrom} rangeTo={jobHistory.rangeTo} /></td>
                    <td>{ new Date(jobHistory.startTime).toUTCString() }</td>
                    <td>{ new Date(jobHistory.finishTime).toUTCString() }</td>
                    <td>{jobHistory.documentsIndexed} / {jobHistory.documentsExpected}</td>
                </tr>
            );
        });

        return (
            <div id="job-history">
                {R.isEmpty(jobHistoryNodes) ?
                    <p>No reindex history. Have you initiated a reindex for this content via Floodgate before?</p>
                    :
                    <Table hover>
                        <thead>
                            <tr>
                                <th>Status</th>
                                <th>Date Range</th>
                                <th>Start Time</th>
                                <th>Finish Time</th>
                                <th>Documents Indexed</th>
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
