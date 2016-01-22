import React from 'react';
import { Button, ButtonToolbar, Input, Alert } from 'react-bootstrap';
import ContentSourceService from '../services/contentSourceService';

export default class ContentSourceEdit extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            appName: this.props.appName,
            description: this.props.description,
            reindexEndpoint: this.props.reindexEndpoint,
            alertStyle: 'success',
            alertMessage: '',
            alertVisibility: false};
        this.exitEditMode = this.exitEditMode.bind(this);
        this.handleAppNameChange = this.handleAppNameChange.bind(this);
        this.handleDescriptionChange = this.handleDescriptionChange.bind(this);
        this.handleReindexEndpointChange = this.handleReindexEndpointChange.bind(this);
        this.handleSubmit =  this.handleSubmit.bind(this);
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

    exitEditMode() {
        this.props.callbackParent(false);
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
        this.handleFormSubmit(this.props.id, {appName: appName, description: description, reindexEndpoint: reindexEndpoint});
    }

    handleFormSubmit(id, form) {
        ContentSourceService.updateContentSource(id, form).then(response => {
            if(response.status == 200) {
                this.setState({alertVisibility: false});
                this.exitEditMode();
            }
        });
    }

    render () {
        return (
            <div>
                { this.state.alertVisibility ? <Alert bsStyle={this.state.alertStyle}>{this.state.alertMessage}</Alert> : null }
                <form className="form-horizontal" onSubmit={this.handleSubmit}>
                    <Input type="text" label="Application Name*" labelClassName="col-xs-2" wrapperClassName="col-xs-10" value={this.state.appName} onChange={this.handleAppNameChange} />
                    <Input type="text" label="Description*" labelClassName="col-xs-2" wrapperClassName="col-xs-10" value={this.state.description} onChange={this.handleDescriptionChange} />
                    <Input type="text" label="Reindex Endpoint*" labelClassName="col-xs-2" wrapperClassName="col-xs-10" value={this.state.reindexEndpoint} onChange={this.handleReindexEndpointChange} />
                    <ButtonToolbar>
                        <Button bsStyle="primary" className="pull-right" type="submit">Submit</Button>
                    </ButtonToolbar>
                </form>
            </div>
        );
    }
}
