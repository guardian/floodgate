import React from 'react';

export default class OtherComponent extends React.Component {

    constructor(props) {
        super(props);
    }

    render () {
        return (
            <div className="editor">
                <h2>Other display.</h2>
                {this.props.children}
            </div>
        );
    }
}