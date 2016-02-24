import React from 'react';
import { Input, Button, ButtonToolbar, Alert, Col } from 'react-bootstrap';
import ContentSourceService from '../services/contentSourceService';

export default class ContentSourceForm extends React.Component {

    constructor(props) {
        super(props);
        this.render = this.render.bind(this);
        this.state = {appName: '', description: '', reindexEndpoint: '', environment: '', alertStyle: 'success', alertMessage: '', alertVisibility: false};
        this.resetFormFields = this.resetFormFields.bind(this);
    }

    handleFormSubmit(form) {
        ContentSourceService.createContentSource(form).then(response => {
            this.resetFormFields();
            this.setState({alertVisibility: false});
        });
    }

    resetFormFields() {
        this.setState({appName: '', description: '', reindexEndpoint: ''});
    }

    handleAppNameChange(e) {
        this.setState({appName: e.target.value});
    }

    handleDescriptionChange(e) {
        this.setState({description: e.target.value});
    }

    handleReindexEndpointChange(e) {
        this.setState({reindexEndpoint: e.target.value});
    }

    handleEnvironmentChange(e) {
        console.log(e.target.value);
        this.setState({environment: e.target.value});
    }

    handleSubmit(e) {
        e.preventDefault();
        var appName = this.state.appName.trim();
        var description = this.state.description.trim();
        var reindexEndpoint = this.state.reindexEndpoint.trim();
        var environment = this.state.environment.trim();
        if(!appName || !description || !reindexEndpoint || !environment) {
            this.setState({alertStyle: 'danger', alertMessage: 'Invalid form. Correct the fields and try again.', alertVisibility: true});
            return;
        }
        this.handleFormSubmit({appName: appName, description: description, reindexEndpoint: reindexEndpoint, environment: environment});
    }

    render () {
        return (
            <div>
                { this.state.alertVisibility ? <Alert bsStyle={this.state.alertStyle}>{this.state.alertMessage}</Alert> : null }
                <form className="form-horizontal" onSubmit={this.handleSubmit.bind(this)}>
                    <Input type="text" label="Application Name*" labelClassName="col-xs-2" wrapperClassName="col-xs-10" value={this.state.appName} onChange={this.handleAppNameChange.bind(this)} />
                    <Input type="text" label="Description*" labelClassName="col-xs-2" wrapperClassName="col-xs-10" value={this.state.description} onChange={this.handleDescriptionChange.bind(this)} />

                    <Input label="Endpoint*" labelClassName="col-xs-2" wrapperClassName="wrapper">
                            <Col xs={4}>
                                <Input type="select" onChange={this.handleEnvironmentChange.bind(this)} labelClassName="col-xs-2" wrapperClassName="col-xs-10" select>
                                    <option value="" selected disabled>Please select environment... </option>
                                    <option value="live-code">Code [live]</option>
                                    <option value="draft-code">Code [draft]</option>
                                    <option value="live-prod">Prod [live]</option>
                                    <option value="draft-prod">Prod [draft]</option>
                                </Input>
                            </Col>
                            <Col xs={6}>
                                <input type="text" value={this.state.reindexEndpoint} onChange={this.handleReindexEndpointChange.bind(this)} placeholder="URL to initiate reindex for content source ..." wrapperClassName="col-xs-4" className="form-control" />
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