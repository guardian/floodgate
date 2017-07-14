import React from 'react';
import R from 'ramda';
import { Link } from 'react-router';
import { Navbar, Nav, NavItem, NavDropdown, MenuItem, Label } from 'react-bootstrap'

export default class NavigationComponent extends React.Component {

    constructor(props) {
        super(props);
    }

    render () {
        const allContentSources = this.props.data;
        const distinctKeys = R.uniq(allContentSources.map(R.prop('id')));

        const contentSourceNodes = distinctKeys.map( itemKey => {
            const contentSource = R.find(R.propEq('id', itemKey), allContentSources);
            const appName = R.prop('appName', contentSource);
            const environment = R.prop('environment', contentSource);
            const route = '#/reindex/' + itemKey + '/environment/' + environment;

            return (
                <MenuItem eventKey={itemKey} key={itemKey} href={route}>{appName}</MenuItem>
            );
        });

        return (
            <Navbar inverse>
                <Navbar.Header>
                    <Navbar.Brand><Link className="navbar-brand" to="/">Floodgate</Link></Navbar.Brand>
                    <Navbar.Toggle />
                </Navbar.Header>
                <Navbar.Collapse>
                    <Nav>
                        <NavItem eventKey={1} href="#/register">Register</NavItem>
                        <NavDropdown eventKey={2} title="Content sources" id="nav-content-source-dropdown">
                            {contentSourceNodes}
                        </NavDropdown>
                        <NavItem eventKey={3} href="#/test">Test</NavItem>
                    </Nav>
                </Navbar.Collapse>
            </Navbar>
        );
    }
}