import React from 'react';
import BulkReindex from './bulkReindexes.react';
import ContentSourceService from '../services/contentSourceService';
import { Label, Row, Col, Panel, Input, Button, ButtonToolbar, Alert } from 'react-bootstrap';

export default class BulkReindexControllerComponent extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            contentSources: [],
            runningReindexes: [],
            pendingReindexes: [],
            completedReindexes: [],
            editModeOn: false,
            inBulkMode: false,
            reindexLiveStack: true,
            reindexPreviewStack: true,
            reindexProd: true,
            reindexCode: true,
            alertStyle: 'success',
            alertMessage: '',
            alertVisibility: false
        };

        this.generateEnvironments = this.generateEnvironments.bind(this);
        this.requestBulkStatus = this.requestBulkStatus.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
        this.initiateBulkReindex = this.initiateBulkReindex.bind(this);
        this.checkRunningReindexes = this.checkRunningReindexes.bind(this);
        this.handleReindexLiveStack = this.handleReindexLiveStack.bind(this);
        this.handleReindexPreviewStack = this.handleReindexPreviewStack.bind(this);
        this.handleReindexCode = this.handleReindexCode.bind(this);
        this.handleReindexProd = this.handleReindexProd.bind(this);
        this.cancelReindex = this.cancelReindex.bind(this);
    }

    componentDidMount() {
        let intervalPeriod = 5000;
        setInterval(this.checkRunningReindexes, intervalPeriod);
    }

    initiateBulkReindex(environments) {
        if (environments.length > 0) {
            ContentSourceService.initiateBulkReindexer(environments).then (response => {
                this.checkRunningReindexes()
                },
            errors => {
                this.setState({alertStyle: 'danger', alertMessage: 'Failed to trigger reindexes for '+ environments, alertVisibility: true});
            });
        } else {
            console.log("Error: No content selected to be reindexed.")
        }
    }

    requestBulkStatus() {
        return ContentSourceService.requestBulkStatus();
    }

    generateEnvironments() {
        const desiredEnvironments = [
            this.state.reindexPreviewStack && this.state.reindexCode ? "draft-code" : "",
            this.state.reindexPreviewStack && this.state.reindexProd ? "draft-prod" : "",
            this.state.reindexLiveStack && this.state.reindexCode ? "live-code" : "",
            this.state.reindexLiveStack && this.state.reindexProd ? "live-prod" : ""
        ].filter(Boolean);
        return desiredEnvironments;
    }

    loadRunningReindexes(json) {

        const r = (acc, v) => acc.concat(v);
        const f = (s) => (o) => o[s];
        const getPendingJobs = f('pendingJobs');
        const getRunningJobs = f('runningJobs');
        const getCompletedJobs = f('completedJobs');

        let pendingReindexes = Object.values(json.data).map(getPendingJobs).reduce(r, []);
        let runningReindexes = Object.values(json.data).map(getRunningJobs).reduce(r, []);
        let completedReindexes = Object.values(json.data).map(getCompletedJobs).reduce(r, []);

        this.setState({
                runningReindexes: runningReindexes,
                pendingReindexes: pendingReindexes,
                completedReindexes: completedReindexes,
                alertVisibility: false,
            });

        this.requestBulkStatus();
    }

    checkRunningReindexes() {
        this.requestBulkStatus().then(response => {
            let json = JSON.parse(response);
            let reindexInProgress =  json.IsReindexing;
            if (!reindexInProgress) {
                this.setState({
                    inBulkMode: false
                });
            } else {
                this.loadRunningReindexes(json);
                this.setState({
                    inBulkMode: true
                });
            }
        });
    }

    cancelReindex(reindex, isReindexRunning) {
        if (isReindexRunning) {
            ContentSourceService.cancelReindex(reindex.id, reindex.env).then( response => {
                    this.setState({
                        runningReindexes: this.state.runningReindexes.filter(r => r !== reindex)
                    });
                },
                errors => {
                    this.setState({alertStyle: 'danger', alertMessage: 'Failed to cancel running reindex for '+ reindex.name, alertVisibility: true});
                });
        } else {
            ContentSourceService.cancelPendingReindex(reindex.id, reindex.env).then( response => {
                this.setState({
                    pendingReindexes: this.state.pendingReindexes.filter(r => r !== reindex)
                });
            },
                errors => {
                    this.setState({alertStyle: 'danger', alertMessage: 'Failed to cancel pending reindex for ' + reindex.name, alertVisibility: true});
                });
        }
    }

    handleSubmit(e) {
        e.preventDefault();
        this.requestBulkStatus().then(response => {
            var json = JSON.parse(response);
            var reindexInProgress =  json.IsReindexing;
            if (!reindexInProgress) {
                let environments = this.generateEnvironments();
                this.setState({alertStyle: 'success', alertMessage: 'Bulk job submitted.', alertVisibility: true});
                this.initiateBulkReindex(environments);
            } else {
                this.setState({alertStyle: 'danger', alertMessage: 'Bulk job in progress. Please wait till job has finished.', alertVisibility: true});
            }
        });
    }

    handleReindexLiveStack(e) {
        this.setState({reindexLiveStack: e.target.checked});
    }

    handleReindexPreviewStack(e) {
        this.setState({reindexPreviewStack: e.target.checked});
    }

    handleReindexProd(e) {
        this.setState({reindexProd: e.target.checked});
    }

    handleReindexCode(e) {
        this.setState({reindexCode: e.target.checked});
    }

    render () {

        return (
            <div id="page-wrapper">
                <div className="container-fluid">
                    <div>
                        { this.state.alertVisibility ? <Alert bsStyle={this.state.alertStyle}>{this.state.alertMessage}</Alert> : null }
                        <form className="form-horizontal" onSubmit={this.handleSubmit}>
                            <Row>
                                <Col xs={12} md={12}>
                                    <h3><Label>Bulk Reindexer</Label></h3>
                                    <Panel header="Start Reindex">
                                        <Input label="Stack" labelClassName="col-xs-2" wrapperClassName="wrapper">
                                            <Col xs={5}>
                                                <Input type="checkbox" defaultChecked={this.state.reindexLiveStack} onChange={this.handleReindexLiveStack} label="Live" />
                                            </Col>
                                            <Col xs={5}>
                                                <Input type="checkbox" defaultChecked={this.state.reindexPreviewStack} onChange={this.handleReindexPreviewStack} label="Preview" />
                                            </Col>
                                        </Input>
                                        <Input label="Stage" labelClassName="col-xs-2" wrapperClassName="wrapper">
                                            <Col xs={5}>
                                                <Input type="checkbox" defaultChecked={this.state.reindexProd} onChange={this.handleReindexProd} label="PROD" />
                                            </Col>
                                            <Col xs={5}>
                                                <Input type="checkbox" defaultChecked={this.state.reindexCode} onChange={this.handleReindexCode} label="CODE" />
                                            </Col>
                                        </Input>
                                        <ButtonToolbar>
                                            <Button bsStyle="success" className="pull-right" type="submit">Reindex</Button>
                                        </ButtonToolbar>
                                    </Panel>
                                </Col>
                            </Row>
                        </form>
                    </div>
                    <div>
                        <Row>
                            <Col xs={12} md={12}>
                                    { (!this.state.inBulkMode) ?
                                        <p>There are no bulk reindexes currently in progress.</p>
                                    :
                                        <BulkReindex completedReindexes={this.state.completedReindexes}
                                                       runningReindexes={this.state.runningReindexes}
                                                       pendingReindexes={this.state.pendingReindexes}
                                                       onReloadRunningReindex={this.checkRunningReindexes}
                                                       onCancelReindex={this.cancelReindex}/>
                                    }
                            </Col>
                        </Row>
                    </div>
                </div>
            </div>
        );
    }
}
