import React from 'react';
import { Input, Button, ButtonToolbar, Alert } from 'react-bootstrap';
import ContentSourceService from '../services/contentSourceService';

export default class ContentSourceForm extends React.Component {

    constructor(props) {
        super(props);
        this.render = this.render.bind(this);
        this.state = {appName: '', description: '', reindexEndpoint: '', alertStyle: 'success', alertMessage: '', alertVisibility: false};
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

    handleSubmit(e) {
        e.preventDefault();
        var appName = this.state.appName.trim();
        var description = this.state.description.trim();
        var reindexEndpoint = this.state.reindexEndpoint.trim();
        if(!appName || !description || !reindexEndpoint) {
            this.setState({alertStyle: 'danger', alertMessage: 'Invalid form. Correct the fields and try again.', alertVisibility: true});
            return;
        }
        this.handleFormSubmit({appName: appName, description: description, reindexEndpoint: reindexEndpoint});
    }

    render () {
        return (
            <div>
                { this.state.alertVisibility ? <Alert bsStyle={this.state.alertStyle}>{this.state.alertMessage}</Alert> : null }
                <form className="form-horizontal" onSubmit={this.handleSubmit.bind(this)}>
                    <Input type="text" label="Application Name*" labelClassName="col-xs-2" wrapperClassName="col-xs-10" value={this.state.appName} onChange={this.handleAppNameChange.bind(this)} />
                    <Input type="text" label="Description*" labelClassName="col-xs-2" wrapperClassName="col-xs-10" value={this.state.description} onChange={this.handleDescriptionChange.bind(this)} />
                    <Input type="text" label="Reindex Endpoint*" labelClassName="col-xs-2" wrapperClassName="col-xs-10" value={this.state.reindexEndpoint} onChange={this.handleReindexEndpointChange.bind(this)} />
                    <ButtonToolbar>
                        <Button bsStyle="success" className="pull-right" type="submit">Submit</Button>
                    </ButtonToolbar>
                </form>
            </div>
        );
    }
}