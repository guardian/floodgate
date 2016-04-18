import React from 'react';
import R from 'ramda';


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

    render () {
        if (this.empty(this.props.rangeFrom) && this.empty(this.props.rangeTo)) {
            return <span>All time</span>;
        } else if (this.nonEmpty(this.props.rangeFrom) && this.empty(this.props.rangeTo)) {
            return <span>{ new Date(this.props.rangeFrom).toISOString().slice(0,10) + " - now" }</span>;
        } else if (this.empty(this.props.rangeFrom) && this.nonEmpty(this.props.rangeTo)) {
            return <span>{ "Start of time - " + new Date(this.props.rangeTo).toISOString().slice(0,10) }</span>;
        } else {
          return <span>{ new Date(this.props.rangeFrom).toISOString().slice(0,10) + " - " + new Date(this.props.rangeTo).toISOString().slice(0,10) }</span>;
        }
    }
}
