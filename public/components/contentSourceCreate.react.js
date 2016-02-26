import React from 'react';
import { Input, Button, ButtonToolbar, Alert, Col } from 'react-bootstrap';
import ContentSourceService from '../services/contentSourceService';

export default class ContentSourceForm extends React.Component {

    constructor(props) {
        super(props);
        this.render = this.render.bind(this);
        this.state = {
            appName: '',
            description: '',
            reindexEndpoint: '',
            environment: '',
            authType: '',
            alertStyle: 'success',
            alertMessage: '',
            alertVisibility: false
        };
        this.resetFormFields = this.resetFormFields.bind(this);
    }

    handleFormSubmit(form) {
        ContentSourceService.createContentSource(form).then(response => {
            this.resetFormFields();
            this.setState({alertVisibility: false});
        });
    }

    resetFormFields() {
        this.setState({appName: '', description: '', reindexEndpoint: '', environment: '', authType: ''});
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
        this.setState({environment: e.target.value});
    }

    handleAuthTypeChange(e) {
        this.setState({authType: e.target.value});
    }

    handleSubmit(e) {
        e.preventDefault();
        var appName = this.state.appName.trim();
        var description = this.state.description.trim();
        var reindexEndpoint = this.state.reindexEndpoint.trim();
        var environment = this.state.environment.trim();
        var authType = this.state.authType.trim();

        if(appName && description && reindexEndpoint && environment && authType ) {
            this.handleFormSubmit({
                appName: appName,
                description: description,
                reindexEndpoint: reindexEndpoint,
                environment: environment,
                authType: authType
            });
        } else {
            this.setState({alertStyle: 'danger', alertMessage: 'Invalid form. Correct the fields and try again.', alertVisibility: true});
            return;
        }
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
                                    <option value="" selected disabled>Select environment ... </option>
                                    <option value="live-code">Code [live]</option>
                                    <option value="draft-code">Code [draft]</option>
                                    <option value="live-prod">Prod [live]</option>
                                    <option value="draft-prod">Prod [draft]</option>
                                </Input>
                            </Col>
                            <Col xs={6}>
                                <input type="text" value={this.state.reindexEndpoint} onChange={this.handleReindexEndpointChange.bind(this)} placeholder="URL for reindex (include api key parameter if required) ..." wrapperClassName="col-xs-4" className="form-control" />
                            </Col>
                    </Input>

                    <Input label="Authentication*" labelClassName="col-xs-2" wrapperClassName="wrapper">
                        <Col xs={4}>
                            <Input type="select" onChange={this.handleAuthTypeChange.bind(this)} labelClassName="col-xs-2" wrapperClassName="col-xs-10" select>
                                <option value="" selected disabled>Select authentication type ... </option>
                                <option value="api-key">Api key</option>
                                <option value="vpc-peered">VPC peered</option>
                            </Input>
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