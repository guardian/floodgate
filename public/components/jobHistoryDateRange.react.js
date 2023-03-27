import React from 'react';
import * as R from 'ramda';
import Moment from 'moment';


export default class JobHistoryDateRange extends React.Component {

    constructor(props) {
        super(props);
    }

    empty(value) {
        return R.isNil(value) || R.isEmpty(value);
    }

    nonEmpty(value) {
        return !this.empty(value);
    }

    date(epochMillis) {
        return Moment(epochMillis).format("YYYY-MM-DD");
    }

    render () {
        if (this.empty(this.props.rangeFrom) && this.empty(this.props.rangeTo)) {
            return <span>All time</span>;
        } else if (this.nonEmpty(this.props.rangeFrom) && this.empty(this.props.rangeTo)) {
            return <span>{ this.date(this.props.rangeFrom) + " - now" }</span>;
        } else if (this.empty(this.props.rangeFrom) && this.nonEmpty(this.props.rangeTo)) {
            return <span>{ "Start of time - " + this.date(this.props.rangeTo) }</span>;
        } else {
            return <span>{ this.date(this.props.rangeFrom) + " - " + this.date(this.props.rangeTo) }</span>;
        }
    }
}
