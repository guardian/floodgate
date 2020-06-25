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
            progress: this.computeProgress( this.props ),
            progressUpdatesEnabled: true
        });
    }

    componentWillReceiveProps(nextProps) {
        this.setState({
            progress: this.computeProgress( nextProps ),
            progressUpdatesEnabled: true
        });
    }

    updateRunningReindex() {
        const contentSourceId = this.props.id;
        const environment = this.props.environment;
        this.props.onReloadRunningReindex(contentSourceId, environment);
    }

    computeProgress(reindex) {
        const waitMins = 15;
        let progress = 0;
        // if documentsExpected is 0, the server hasn't yet started the reindex, if it is non-zero
        // we'll display the actual reindex progress.
        if (reindex.documentsExpected !== 0) {
            progress = reindex.documentsIndexed / reindex.documentsExpected * 100;
        } else {
            // Express progress as a waitMins-derived percentage so that the progress bar
            // fills to 100% at start and ticks down towards 0%
            const elapsedMins = (new Date().valueOf() - new Date(reindex.startTime).valueOf()) / 60000;
            progress = -100 + (elapsedMins * (100 / waitMins));
            // At this point the reindex has failed to start within 15 mins. Setting progress to 101
            // means we can re-fill the progress bar, re-display the cancel button and display a
            // meaningful message. It will remain in this state until either the reindex starts or
            // it is cancelled by the user. It's clunky and this entire component could do with some
            // additional thought but it's better than we have currently.
            progress = (progress > -1) ? -101 : progress;
        }
        return progress;
    }

    cancelReindex() {
        const runningReindex = this.props.reindex;
        this.props.onCancelReindex(runningReindex);
    }

    renderCancelButton(progress) {
        // Only display the cancel button if the reindex is running or has failed to start within
        // our expected time frame (15 mins)
        if (this.props.isCancelSupported && (progress >= 0 || progress < -100)) {
            return (
                <Button bsStyle="danger" className="pull-right" type="button" onClick={this.cancelReindex.bind(this)}>Cancel</Button>
            );
        } else {
            return (null);
        }
    }


    render () {

        var pollFreq = 5000;    // 5s polling for less aggressive effects on indexing services
        let fgColour = "black";
        let progressVariant = "info";
        let labelText = "%(percent)s%";
        let barValue = Math.round(Math.abs(this.state.progress));

        switch (true) {
            case (this.state.progress < -100):  // this happens when we're past max expected wait minutes
                fgColour = "white";
                progressVariant = "danger";
                labelText = "The reindex has failed to start - you may continue to wait or attempt to cancel it";
                barValue = 100;
                break;
            case (this.state.progress < -65):
                progressVariant = "success";
                labelText = "Waiting for server - please be patient as this can take a while... " + barValue;
                break;
            case (this.state.progress < -22):
                progressVariant = "warning";
                labelText = "Waiting for server... " + barValue;
                break;
            case (this.state.progress < -10):
                fgColour = "white";
                progressVariant = "danger";
                labelText = "... " + barValue;
                break;
            case (this.state.progress < 0):
                fgColour = "white";
                progressVariant = "danger";
                labelText = barValue;
                break;
        }

        return (
            <div key={this.props.startTime}>
                <ReactInterval timeout={pollFreq} enabled={this.state.progressUpdatesEnabled}
                               callback={ this.updateRunningReindex.bind(this) } />
                <ProgressBar striped
                             active
                             now={barValue}
                             label={labelText}
                             style={{ color: fgColour }}
                             bsStyle={progressVariant}
                />
                { this.renderCancelButton(this.state.progress) }
            </div>
        );
    }
}
