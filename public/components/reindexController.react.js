import React from 'react';
import ContentSource from './contentSource.react';
import ContentSourceEdit from './contentSourceEdit.react';
import JobHistory from './jobHistory.react';
import RunningReindex from './runningReindex.react';
import ReindexForm from './reindexForm.react';
import ContentSourceService from '../services/contentSourceService';
import { Label, Row, Col, Panel, ProgressBar, Nav, NavItem } from 'react-bootstrap';

export default class ReindexControllerComponent extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            contentSource: {},
            contentSourcesForEnvironments: [],
            reindexHistory: [],
            runningReindex: {},
            editModeOn: false
        };

        this.updateEditModeState = this.updateEditModeState.bind(this);
        this.loadRunningReindex = this.loadRunningReindex.bind(this);
        this.loadContentSourceWithId = this.loadContentSourceWithId.bind(this);
    }

    componentDidMount() {
        var contentSourceId = this.props.params.id;
        var environment = this.props.params.environment;
        this.loadReindexHistory(contentSourceId, environment);
        this.loadRunningReindex(contentSourceId, environment);
        this.loadContentSourceWithId(contentSourceId, environment);
    }

    componentWillReceiveProps(nextProps) {
        if (this.props.params.id !== nextProps.params.id || this.props.params.environment !== nextProps.params.environment) {
            this.loadReindexHistory(nextProps.routeParams.id, nextProps.routeParams.environment);
            this.loadRunningReindex(nextProps.routeParams.id, nextProps.routeParams.environment);
            this.loadContentSourceWithId(nextProps.routeParams.id, nextProps.routeParams.environment);
            this.setState({editModeOn: false});
        }
    }

    loadContentSourceWithId(id, environment) {
        ContentSourceService.getContentSourcesWithId(id).then(response => {
            var contentSources = response.contentSources.reverse();
            this.setState({
                contentSourcesForEnvironments: contentSources
            }, function() {
                contentSources.forEach( contentSource => {
                    if(contentSource.environment === environment) {
                        this.setState({
                            contentSource: contentSource
                        });
                    }
                });
            });
        });
    }

    loadReindexHistory(contentSourceId, environment) {
        ContentSourceService.getReindexHistory(contentSourceId, environment).then(response => {
            this.setState({
                reindexHistory: response.jobHistories
            });
        });
    }

    loadRunningReindex(contentSourceId, environment) {
        ContentSourceService.getRunningReindex(contentSourceId, environment).then(response => {

            this.setState({
                runningReindex: response.runningJob
            });
        },
        error => {
            this.loadReindexHistory(contentSourceId, environment);
            this.setState({
                runningReindex: {}
            });
        });
    }

    initiateReindex(contentSourceId, environment, startDate, endDate) {
        ContentSourceService.initiateReindex(contentSourceId, environment, startDate, endDate).then( response => {
            this.loadRunningReindex(contentSourceId, environment);
        },
        error => {
            console.log(error.response);
        })
    }

    cancelReindex(currentRunningReindex) {
        var newReindexHistoryItem = { contentSourceId: currentRunningReindex.contentSourceId,
            environment: currentRunningReindex.contentSourceEnvironment, status: 'cancelled',
            startTime: currentRunningReindex.startTime, finishTime: new Date() };

        ContentSourceService.cancelReindex(currentRunningReindex.contentSourceId, currentRunningReindex.contentSourceEnvironment).then( response => {
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
        if(newState == false) this.loadContentSourceWithId(this.props.params.id, this.props.params.environment);
    }

    render () {

        var environmentNodes = this.state.contentSourcesForEnvironments.map(function(contentSource){
            var itemEnvironment = contentSource.environment;
            var itemKey = contentSource.id;
            var route = '#/reindex/' + itemKey + '/environment/' + itemEnvironment;
            return(
                <NavItem key={itemKey + '-' + itemEnvironment} eventKey={itemEnvironment} href={route}>{itemEnvironment}</NavItem>
            )
        });

        return (
            <div id="page-wrapper">
                <div className="container-fluid">
                    <Row>
                        <Col xs={12} md={12}>
                            <h3><Label>{this.state.contentSource.appName} Reindexer</Label></h3>
                        </Col>
                        <Col xs={12} md={12}>
                            <Panel>
                                <Nav bsStyle="pills" activeKey={this.props.params.environment}>
                                    {environmentNodes}
                                </Nav>
                            </Panel>
                        </Col>
                        <Col xs={12} md={5}>
                            <Panel header="Details">
                                {this.state.editModeOn  ?
                                    <ContentSourceEdit key={this.state.contentSource.id}
                                        contentSource={this.state.contentSource}
                                        callbackParent={this.updateEditModeState} />
                                    :
                                    this.state.contentSource.contentSourceSettings != undefined ?
                                        <ContentSource key={this.state.contentSource.id}
                                            contentSource={this.state.contentSource}
                                            callbackParent={this.updateEditModeState}/>
                                        : null
                                }
                            </Panel>

                            <Panel header="Start Reindex">
                                {this.state.contentSource.contentSourceSettings != undefined ?
                                    <ReindexForm key={this.state.contentSource.id}
                                        contentSource={this.state.contentSource}
                                        onInitiateReindex={this.initiateReindex.bind(this)}/>
                                    : null
                                }
                            </Panel>
                        </Col>

                        <Col xs={12} md={7}>
                            <Panel header="Running Reindexes">
                                {this.state.runningReindex === undefined ||
                                 Object.keys(this.state.runningReindex).length === 0 ||
                                 this.state.contentSource.contentSourceSettings === undefined ||
                                 Object.keys(this.state.contentSource).length === 0 ?
                                    <p>There are no reindexes currently in progress.</p>
                                    :
                                    <RunningReindex data={this.state.runningReindex}
                                                    isCancelSupported={this.state.contentSource.contentSourceSettings.supportsCancelReindex}
                                                    onCancelReindex={this.cancelReindex.bind(this)}
                                                    onReloadRunningReindex={this.loadRunningReindex.bind(this)}/>
                                }
                            </Panel>
                        </Col>

                        <Col xs={12} md={7}>
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