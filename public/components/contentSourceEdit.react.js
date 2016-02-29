import React from 'react';
import { Button, ButtonToolbar, Input, Alert, Col } from 'react-bootstrap';
import ContentSourceService from '../services/contentSourceService';

export default class ContentSourceEdit extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            appName: this.props.contentSource.appName,
            description: this.props.contentSource.description,
            reindexEndpoint: this.props.contentSource.reindexEndpoint,
            authType: this.props.contentSource.authType,
            supportsToFromParams: this.props.contentSource.contentSourceSettings.supportsToFromParams,
            supportsCancelReindex: this.props.contentSource.contentSourceSettings.supportsCancelReindex,
            alertStyle: 'success',
            alertMessage: '',
            alertVisibility: false
        };
        this.exitEditMode = this.exitEditMode.bind(this);
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

    handleAuthTypeChange(e) {
        this.setState({authType: e.target.value});
    }

    handleSupportsToFromParams(e) {
        this.setState({supportsToFromParams: e.target.checked});

    }

    handleSupportsCancelReindex(e) {
        this.setState({supportsCancelReindex: e.target.checked});
    }

    exitEditMode() {
        this.props.callbackParent(false);
    }

    handleSubmit(e) {
        e.preventDefault();
        var appName = this.state.appName.trim();
        var description = this.state.description.trim();
        var reindexEndpoint = this.state.reindexEndpoint.trim();
        var authType = this.state.authType.trim();
        var supportsToFromParams = this.state.supportsToFromParams;
        var supportsCancelReindex = this.state.supportsCancelReindex;

        if(appName && description && reindexEndpoint && authType && supportsToFromParams != undefined && supportsCancelReindex != undefined) {
            this.handleFormSubmit(this.props.contentSource.id, this.props.contentSource.environment,
                {appName: appName,
                 description: description,
                 reindexEndpoint: reindexEndpoint,
                 authType: authType,
                 contentSourceSettings: {
                     supportsToFromParams: supportsToFromParams,
                     supportsCancelReindex: supportsCancelReindex
                 }});
        } else {
            this.setState({alertStyle: 'danger', alertMessage: 'Invalid form. Correct the fields and try again.', alertVisibility: true});
            return;
        }
    }

    handleFormSubmit(id, environment, form) {
        ContentSourceService.updateContentSource(id, environment, form).then(response => {
            this.setState({alertVisibility: false});
            this.exitEditMode();
        });
    }

    render () {
        return (
            <div>
                { this.state.alertVisibility ? <Alert bsStyle={this.state.alertStyle}>{this.state.alertMessage}</Alert> : null }
                <form className="form-horizontal" onSubmit={this.handleSubmit.bind(this)}>
                    <Input type="text" label="Application Name*" labelClassName="col-xs-2" wrapperClassName="col-xs-10" value={this.state.appName} onChange={this.handleAppNameChange.bind(this)} />
                    <Input type="text" label="Description*" labelClassName="col-xs-2" wrapperClassName="col-xs-10" value={this.state.description} onChange={this.handleDescriptionChange.bind(this)} />
                    <Input type="text" label="Reindex Endpoint*" labelClassName="col-xs-2" wrapperClassName="col-xs-10" value={this.state.reindexEndpoint} onChange={this.handleReindexEndpointChange.bind(this)} />

                    <Input type="select" value={this.state.authType} label="Auth Type*" onChange={this.handleAuthTypeChange.bind(this)} labelClassName="col-xs-2" wrapperClassName="col-xs-10" select>
                        <option value="" selected disabled>Select authentication type ... </option>
                        <option value="api-key">Api key</option>
                        <option value="vpc-peered">VPC peered</option>
                    </Input>

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
                        <Button bsStyle="danger" className="pull-right" type="button" onClick={this.exitEditMode}>Cancel</Button>
                    </ButtonToolbar>
                </form>
            </div>
        );
    }
}
