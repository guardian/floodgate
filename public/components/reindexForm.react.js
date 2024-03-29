import React from 'react';
import * as R from 'ramda';
import { Button, ButtonToolbar, Alert, Input, Row, Col } from 'react-bootstrap';
import Moment from 'moment';

export default class ReindexForm extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            startDate: Moment().format('YYYY-MM-DD'),
            endDate: Moment().format('YYYY-MM-DD'),
            alertStyle: 'success',
            alertMessage: '',
            alertVisibility: false};
    }

    initiateReindex() {
        const isToFromSupported = this.props.contentSource.contentSourceSettings.supportsToFromParams;
        const id = this.props.contentSource.id;
        const environment = this.props.contentSource.environment;
        const startDate = R.isEmpty(this.state.startDate) || !isToFromSupported ? '' : Moment(this.state.startDate).toISOString();
        const endDate = R.isEmpty(this.state.endDate) || !isToFromSupported ? '' : Moment(this.state.endDate).endOf('day').toISOString();

        if (Moment(endDate).isBefore(startDate))
            this.setState({
                alertStyle: 'danger',
                alertMessage: 'Invalid dates entered. Please correct and try again.',
                alertVisibility: true});
        else {
            this.setState({alertVisibility: false});
            this.props.onInitiateReindex(id, environment, startDate, endDate);
        }
    }

    handleStartDate(e) {
        const startDate = e.target.value;
        this.setState({
            startDate: startDate
        });
        this.updateDates(startDate, this.state.endDate);
    }

    handleEndDate(e) {
        const endDate = e.target.value;
        this.setState({
            endDate: endDate
        });
        this.updateDates(this.state.startDate, endDate);
    }

    handleAlertDismiss() {
        this.setState({alertVisibility: false});
    }

    updateDates(start, end) {
        this.setState({startDate: start, endDate: end});
    }

    renderToFromParams() {
        if (!this.props.contentSource.contentSourceSettings.supportsToFromParams) {
            return (
                <Alert bsStyle="info">Reindexing for a specific period of time is not supported with this content source. You may only reindex <strong>all</strong>.</Alert>
            );
        } else {
            return (
                <Row>
                    <Col xs={6}>
                        <Input type="date" label="Start date" value={this.state.startDate} onChange={this.handleStartDate.bind(this)} />
                    </Col>
                    <Col xs={6}>
                        <Input type="date" label="End Date" value={this.state.endDate} onChange={this.handleEndDate.bind(this)} />
                    </Col>
                </Row>
            );
        }
    }

    render () {
        return (
            <div>
                { this.state.alertVisibility ? <Alert bsStyle={this.state.alertStyle} onDismiss={this.handleAlertDismiss.bind(this)}>{this.state.alertMessage}</Alert> : null }
                <form>
                    {this.renderToFromParams()}
                    <Button bsStyle="primary" className="pull-right" onClick={this.initiateReindex.bind(this)}>Reindex</Button>
                </form>
            </div>
        );
    }
}