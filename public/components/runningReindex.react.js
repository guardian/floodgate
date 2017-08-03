import React from 'react';
import ContentSourceService from '../services/contentSourceService';
import { ProgressBar, Button } from 'react-bootstrap';
import ReactInterval from 'react-interval';


export default class RunningReindex extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            progressUpdatesEnabled: false,
            progress: 0
        };

        this.cancelReindex = this.cancelReindex.bind(this);
    }

    componentDidMount() {
        this.setState({
            progress: this.computeProgress( this.props.documentsIndexed, this.props.documentsExpected),
            progressUpdatesEnabled: true
        });
    }

    componentWillReceiveProps(nextProps) {
        this.setState({
            progress: this.computeProgress( nextProps.documentsIndexed, nextProps.documentsExpected),
            progressUpdatesEnabled: true
        });
    }

    updateRunningReindex() {
        const contentSourceId = this.props.id;
        const environment = this.props.environment;
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
        const runningReindex = this.props.reindex;
        this.props.onCancelReindex(runningReindex);
    }

    renderCancelButton() {
        if (this.props.isCancelSupported) {
            return (
                <Button bsStyle="danger" className="pull-right" type="button" onClick={this.cancelReindex.bind(this)}>Cancel</Button>
            );
        } else {
            return (null);
        }
    }


    render () {

        var timeout = 2000;

        return (
            <div key={this.props.startTime}>
                <ReactInterval timeout={this.timeout} enabled={this.state.progressUpdatesEnabled}
                               callback={ this.updateRunningReindex.bind(this) } />
                <ProgressBar striped active now={this.state.progress} label="%(percent)s%"/>

                { this.renderCancelButton() }
            </div>
        );
    }
}
