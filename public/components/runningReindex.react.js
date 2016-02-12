import React from 'react';
import ContentSourceService from '../services/contentSourceService';
import { ProgressBar, Button } from 'react-bootstrap';


export default class RunningReindex extends React.Component {

    constructor(props) {
        super(props);
        this.cancelReindex = this.cancelReindex.bind(this);
    }

    cancelReindex() {
        var runningReindex = this.props.data
        this.props.onCancelReindex(runningReindex);
    }

    render () {
        return (
            <div key={this.props.data.startTime}>
                <ProgressBar striped active now={this.props.data.progress} label="%(percent)s%"/>
                <Button bsStyle="danger" className="pull-right" type="button" onClick={this.cancelReindex.bind(this)}>Cancel</Button>
            </div>
        );
    }
}
