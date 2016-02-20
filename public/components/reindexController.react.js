import React from 'react';
import ContentSource from './contentSource.react';
import ContentSourceEdit from './contentSourceEdit.react';
import JobHistory from './jobHistory.react';
import RunningReindex from './runningReindex.react';
import ReindexForm from './reindexForm.react';
import ContentSourceService from '../services/contentSourceService';
import { Label, Row, Col, Panel, ProgressBar } from 'react-bootstrap';

export default class ReindexControllerComponent extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            contentSource: {},
            reindexHistory: [],
            runningReindex: {},
            editModeOn: false
        };

        this.updateEditModeState = this.updateEditModeState.bind(this);
        this.loadContentSource = this.loadContentSource.bind(this);
        this.loadRunningReindexes = this.loadRunningReindexes.bind(this);
    }

    componentDidMount() {
        var contentSourceId = this.props.params.id;
        this.loadContentSource(contentSourceId);
        this.loadReindexHistory(contentSourceId);
        this.loadRunningReindexes(contentSourceId);
    }

    componentWillReceiveProps(nextProps) {
        if (this.props.params.id !== nextProps.params.id) {
            this.loadContentSource(nextProps.routeParams.id);
            this.loadReindexHistory(nextProps.routeParams.id);
            this.loadRunningReindexes(nextProps.routeParams.id);
            this.setState({editModeOn: false});
        }
    }

    loadContentSource(id) {
        ContentSourceService.getContentSource(id).then(response => {
            this.setState({
                contentSource: response.contentSource
            });
        });
    }

    loadReindexHistory(contentSourceId) {
        ContentSourceService.getReindexHistory(contentSourceId).then(response => {
            this.setState({
                reindexHistory: response.jobHistories
            });
        });
    }

    loadRunningReindexes(contentSourceId) {
        ContentSourceService.getRunningReindexes(contentSourceId).then(response => {
            if (response.runningJobs.length === 0) this.loadReindexHistory(contentSourceId);

            this.setState({
                runningReindex: response.runningJobs[0]
            });
        });
    }

    initiateReindex(contentSourceId, startDate, endDate) {
        ContentSourceService.initiateReindex(contentSourceId, startDate, endDate).then(response => {
            this.loadRunningReindexes(contentSourceId);
        },
        error => {
            console.log(error.response);
        })
    }

    cancelReindex(currentRunningReindex) {
        var newReindexHistoryItem = { contentSourceId: currentRunningReindex.contentSourceId, status: 'cancelled', startTime: currentRunningReindex.startTime, finishTime: new Date() };

        ContentSourceService.cancelReindex(currentRunningReindex.contentSourceId).then( response => {
            // Optimistically add job history and delete running job
            this.setState({
                runningReindex: {},
                reindexHistory: this.state.reindexHistory.concat([newReindexHistoryItem])
            });
        },
        errors => {
            var indexOfItemToDelete = this.state.reindexHistory.findIndex(r => r.contentSourceId === currentRunningReindex.contentSourceId)
            //delete job history and add running job
            this.setState({
                runningReindex: currentRunningReindex,
                reindexHistory: this.state.reindexHistory.splice(indexOfItemToDelete, 1)
            });
    });

    }

    updateEditModeState(newState) {
        this.setState({ editModeOn: newState });
        if(newState == false) this.loadContentSource(this.props.params.id);
    }

    render () {

        return (
            <div id="page-wrapper">
                <div className="container-fluid">
                    <Row>
                        <Col xs={12} md={12}>
                            <h3><Label>{this.state.contentSource.appName} Reindexer</Label></h3>
                        </Col>
                        <Col xs={5} md={5}>
                            <Panel header="Details">
                                {this.state.editModeOn ?
                                    <ContentSourceEdit key={this.state.contentSource.id}
                                        contentSource={this.state.contentSource}
                                        callbackParent={this.updateEditModeState} />
                                    :
                                    <ContentSource key={this.state.contentSource.id}
                                        contentSource={this.state.contentSource}
                                        callbackParent={this.updateEditModeState}/>
                                }
                            </Panel>

                            <Panel header="Start Reindex">
                                <ReindexForm key={this.state.contentSource.id}
                                             contentSource={this.state.contentSource}
                                             onInitiateReindex={this.initiateReindex.bind(this)}/>
                            </Panel>
                        </Col>

                        <Col xs={7} md={7}>
                            <Panel header="Running Reindexes">
                                {this.state.runningReindex === undefined || Object.keys(this.state.runningReindex).length === 0 ?
                                    <p>There are no reindexes currently in progress.</p>
                                    :
                                    <RunningReindex data={this.state.runningReindex}
                                                    onCancelReindex={this.cancelReindex.bind(this)}
                                                    onReloadRunningReindexes={this.loadRunningReindexes.bind(this)}/>
                                }
                            </Panel>
                        </Col>

                        <Col xs={7} md={7}>
                            <Panel header="Reindex History">
                                <JobHistory data={this.state.reindexHistory}/>
                            </Panel>
                        </Col>
                    </Row>
                </div>
            </div>
        );
    }
}