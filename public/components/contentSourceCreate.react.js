import React from 'react';
import update from 'react-addons-update';
import { Input, Button, ButtonToolbar, Alert, Col, Glyphicon, Row } from 'react-bootstrap';
import ContentSourceService from '../services/contentSourceService';
import {headerListToHeaderMap, HeadersForm} from "./headersForm";

export default class ContentSourceForm extends React.Component {
    constructor(props) {
        super(props);
        this.render = this.render.bind(this);
        this.state = {
            appName: '',
            description: '',
            supportsToFromParams: true,
            supportsCancelReindex: true,
            environments: [],
            alertStyle: 'success',
            alertMessage: '',
            alertVisibility: false,
            environmentCount: 1
        };
        this.resetFormFields = this.resetFormFields.bind(this);
        this.resetEnvironments = this.resetEnvironments.bind(this);
        this.addEnvironmentItem = this.addEnvironmentItem.bind(this);
        this.handleFormSubmit = this.handleFormSubmit.bind(this);
    }

    componentDidMount() {
        this.addEnvironmentItem();
    }

    addEnvironmentItem() {
        const newItem = {
            reindexEndpoint: '',
            environment: '',
            authType: ''
        };
        this.setState({
            environments: this.state.environments.concat([newItem])
        });
    }

    deleteEnvironmentItem(id, e) {
        if (this.state.environmentCount === 1) {
            this.setState({alertStyle: 'danger', alertMessage: 'You need at least one environment to submit the content source.', alertVisibility: true});
        }
        else {
            const newState = update(this.state,
                {
                    environments: {$splice: [[id, 1]]}
                }
            );
            this.setState(newState);
            this.setState({environmentCount: this.state.environmentCount - 1});
        }
    }

    handleFormSubmit(form) {
        ContentSourceService.createContentSource(form).then(response => {
            this.resetFormFields();
            this.setState({alertVisibility: false});
        });
    }

    resetFormFields() {
        this.setState({appName: '', description: '', reindexEndpoint: '', environment: '', authType: '', supportsToFromParams: true, supportsCancelReindex: true});
    }

    resetEnvironments() {
        this.setState({environments: [], environmentCount: 1}, function(){
            this.addEnvironmentItem();
        });
    }

    handleAppNameChange(e) {
        this.setState({appName: e.target.value});
    }

    handleDescriptionChange(e) {
        this.setState({description: e.target.value});
    }

    handleEnvironmentsChangeEvent = (id, field, e) => {
        this.handleEnvironmentsChange(id, field, e.target.value)
    }

    handleEnvironmentsChange(id, field, value) {
        const newState = update(this.state, {
            environments: {
                [id]: {
                [field]: { $set: value },
                },
            },
        });
        this.setState(newState);
    }

    handleSupportsToFromParams(e) {
        this.setState({supportsToFromParams: e.target.checked});
    }

    handleSupportsCancelReindex(e) {
        this.setState({supportsCancelReindex: e.target.checked});
    }


    handleSubmit(e) {
        e.preventDefault();
        var appName = this.state.appName.trim();
        var description = this.state.description.trim();
        var environments = this.state.environments;
        var supportsToFromParams = this.state.supportsToFromParams;
        var supportsCancelReindex = this.state.supportsCancelReindex;

        if (appName && description && environments) {
            const toAdd = environments.map( function(obj, id){
                return {
                    appName: appName,
                    description: description,
                    reindexEndpoint: obj.reindexEndpoint,
                    environment: obj.environment,
                    authType: obj.authType,
                    contentSourceSettings: {
                        supportsToFromParams: supportsToFromParams,
                        supportsCancelReindex: supportsCancelReindex
                    },
                    ...(obj.headers ? { headers: headerListToHeaderMap(obj.headers) } : {})
                };
            }, this);

            this.handleFormSubmit(toAdd);
            this.resetEnvironments();
        } else {
            this.setState({alertStyle: 'danger', alertMessage: 'Invalid form. Correct the fields and try again.', alertVisibility: true});
        }
    }

    handleAddEnvironmentClick(e) {
        this.addEnvironmentItem();
        this.setState({environmentCount: this.state.environmentCount + 1})
    }

    render () {
        return (
            <div>
                { this.state.alertVisibility ? <Alert bsStyle={this.state.alertStyle}>{this.state.alertMessage}</Alert> : null }
                <form className="form-horizontal" onSubmit={this.handleSubmit.bind(this)}>
                    <Input type="text" label="Application Name*" labelClassName="col-xs-2" wrapperClassName="col-xs-10" value={this.state.appName} onChange={this.handleAppNameChange.bind(this)} />
                    <Input type="text" label="Description*" labelClassName="col-xs-2" wrapperClassName="col-xs-10" value={this.state.description} onChange={this.handleDescriptionChange.bind(this)} />
                    {this.state.environments.map(function(e, id){
                        return (
                            <Row key={id}>
                                <label className="control-label col-xs-2">Environment*</label>
                                <Col xs={10}>
                                    <div className="margin-bottom-10 panel panel-default">
                                        <div className="panel-body">
                                            <Row>
                                                <Col xs={12}>
                                                    <Input label="Endpoint*" labelClassName="col-xs-2" wrapperClassName="wrapper">
                                                        <Col xs={4}>
                                                            <Input type="select" onChange={this.handleEnvironmentsChangeEvent.bind(this, id, "environment")} labelClassName="col-xs-2" wrapperClassName="col-xs-10" select value={e.environment}>
                                                                <option value="" disabled>Select stage ... </option>
                                                                <option value="live-code">Code [live]</option>
                                                                <option value="draft-code">Code [draft]</option>
                                                                <option value="live-prod">Prod [live]</option>
                                                                <option value="draft-prod">Prod [draft]</option>
                                                            </Input>
                                                        </Col>
                                                        <Col xs={6}>
                                                            <input type="text" value={e.reindexEndpoint} onChange={this.handleEnvironmentsChangeEvent.bind(this, id, "reindexEndpoint")} placeholder="URL for reindex (include api key parameter if required) ..." wrapperClassName="col-xs-4" className="form-control" />
                                                        </Col>
                                                    </Input>
                                                </Col>
                                            </Row>

                                            <Row>
                                                <Col xs={12}>
                                                    <Input label="Authentication*" labelClassName="col-xs-2" wrapperClassName="wrapper">
                                                        <Col xs={4} className="no-margin-bottom">
                                                            <Input type="select" onChange={this.handleEnvironmentsChangeEvent.bind(this, id, "authType")} labelClassName="col-xs-2" wrapperClassName="col-xs-10" select value={e.authType}>
                                                                <option value="" disabled>Select authentication type ... </option>
                                                                <option value="api-key">Api key</option>
                                                                <option value="vpc-peered">VPC peered</option>
                                                            </Input>
                                                        </Col>
                                                        <Col xs={6}>
                                                            <Button className="remove-btn pull-right btn btn-link btn-sm" onClick={this.deleteEnvironmentItem.bind(this, id)}><Glyphicon glyph="glyphicon glyphicon-minus" /> Remove</Button>
                                                        </Col>
                                                    </Input>
                                                </Col>
                                            </Row>
                                            <Row>
                                                <Col xs={12}>
                                                    <HeadersForm
                                                        headers={e.headers}
                                                        onChange={(headers) => this.handleEnvironmentsChange(id, "headers", headers)}
                                                    />
                                                </Col>
                                            </Row>
                                        </div>
                                    </div>
                                </Col>
                            </Row>
                        );
                    }, this)}

                    { this.state.environmentCount < 4
                        ? <Row><Col xs={10} xsOffset={2}><Button type="button" className="btn-link" onClick={this.handleAddEnvironmentClick.bind(this)}><Glyphicon glyph="glyphicon glyphicon-plus" /> Add another environment</Button></Col></Row>
                        : null
                    }

                    <Input label="Settings" labelClassName="col-xs-2" wrapperClassName="wrapper">
                        <Col xs={4}>
                            <Input type="checkbox" defaultChecked={this.state.supportsToFromParams} onChange={this.handleSupportsToFromParams.bind(this)} label="Supports to/from reindex parameters" />
                        </Col>
                        <Col xs={4}>
                            <Input type="checkbox" defaultChecked={this.state.supportsCancelReindex} onChange={this.handleSupportsCancelReindex.bind(this)} label="Supports cancelling of a reindex" />
                        </Col>
                    </Input>

                    <ButtonToolbar>
                        <Button bsStyle="success" className="pull-right" type="submit">Submit</Button>
                    </ButtonToolbar>
                </form>
            </div>
        );
    }
}