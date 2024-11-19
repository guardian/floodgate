import {Button, Col, Glyphicon, Input, Row} from "react-bootstrap";
import React from "react";

export const emptyHeader = { key: "", value: ""};

export const headerListToHeaderMap = (headerList) => headerList.reduce((acc, { key, value }) => ({
    ...acc,
    [key]: value
}), {})

/**
 * @param props {{
 *   headers: Array<{key: string, value: string}>,
 *   onChange: (headers: Array<{key: string, value: string}>) => void
 * }}
 */
export class HeadersForm extends React.Component {
    getHeadersOrDefault = () => {
        return this.props.headers ?? [emptyHeader]
    }

    handleHeaderChange = (value, key, index) => {
        const newHeaders = [...this.getHeadersOrDefault()];
        newHeaders[index] = { ...newHeaders[index], [key]: value };
        this.props.onChange(newHeaders)
    }

    addHeader = () => {
        const newHeaders = [...this.getHeadersOrDefault(), emptyHeader];
        this.props.onChange(newHeaders)
    }

    removeHeader = (index) => {
        const newHeaders = this.getHeadersOrDefault().filter((_, localIndex) => localIndex !== index);
        this.props.onChange(newHeaders)
    }

    render = () => {
        const {headers} = this.props;

        return <Input label="Headers" labelClassName="col-xs-2" wrapperClassName="wrapper">
            <Col xs={10}>
                {(headers ?? [emptyHeader]).map(({key, value}, index) =>
                    <Row key={index}>
                        <Col xs={4} className="no-margin-bottom">
                            <input type="text" placeholder="Add a header key"
                                   onChange={(e) => this.handleHeaderChange(e.target.value, "key", index)}
                                   className="form-control" value={key}>
                            </input>
                        </Col>
                        <Col xs={4} className="no-margin-bottom">
                            <input type="text" placeholder="Add a header value"
                                   onChange={(e) => this.handleHeaderChange(e.target.value, "value", index)}
                                   className="form-control" value={value}>
                            </input>
                        </Col>
                        <Col xs={2}>
                            <Button className="remove-btn pull-right btn btn-link btn-sm"
                                    onClick={() => this.removeHeader(index)}><Glyphicon
                                glyph="glyphicon glyphicon-minus"/> Remove</Button>
                        </Col>
                    </Row>
                )}
                <Row>
                    <Col xs={12}>
                        <Button className="remove-btn pull-left btn btn-link btn-sm"
                                onClick={this.addHeader}><Glyphicon
                            glyph="glyphicon glyphicon-plus"/> Add
                            another header</Button>
                    </Col>
                </Row>
            </Col>
        </Input>
    }
}