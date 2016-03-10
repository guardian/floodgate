import React from 'react';
import ContentSourceService from '../services/contentSourceService';
import { ProgressBar, Button } from 'react-bootstrap';
import ReactInterval from 'react-interval';


export default class RunningReindex extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            timeout: 2000,
            progressUpdatesEnabled: false,
            progress: 0
        };

        this.cancelReindex = this.cancelReindex.bind(this);
    }

    componentDidMount() {
        this.setState({
            progress: this.computeProgress( this.props.data.documentsIndexed, this.props.data.documentsExpected),
            progressUpdatesEnabled: true
        });
    }

    componentWillReceiveProps(nextProps) {
        this.setState({
            progress: this.computeProgress( nextProps.data.documentsIndexed, nextProps.data.documentsExpected),
            progressUpdatesEnabled: true
        });
    }

    updateRunningReindex() {
        const contentSourceId = this.props.data.contentSourceId;
        const environment = this.props.data.contentSourceEnvironment;
        this.props.onReloadRunningReindex(contentSourceId, environment);
    }

    computeProgress(documentsIndexed, documentsExpected) {
        let progress = 0;

        if (documentsExpected != 0) {
            progress = documentsIndexed / documentsExpected * 100;
        }
        return progress;
    }

    cancelReindex() {
        const runningReindex = this.props.data;
        this.props.onCancelReindex(runningReindex);
    }


    render () {

        return (
            <div key={this.props.data.startTime}>
                <ReactInterval timeout={this.state.timeout} enabled={this.state.progressUpdatesEnabled}
                               callback={ this.updateRunningReindex.bind(this) } />
                <ProgressBar striped active now={this.state.progress} label="%(percent)s%"/>

                {this.props.isCancelSupported ?
                    <Button bsStyle="danger" className="pull-right" type="button" onClick={this.cancelReindex.bind(this)}>Cancel</Button>
                    : null
                }
            </div>
        );
    }
}
