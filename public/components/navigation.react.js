import React from 'react';
import { Link } from 'react-router';
import { Navbar, Nav, NavItem, NavDropdown, MenuItem } from 'react-bootstrap'

export default class NavigationComponent extends React.Component {

    constructor(props) {
        super(props);
    }

    render () {

        var contentSourceNodes = this.props.data.map(function(contentSource) {
            var itemKey = contentSource.id + '-' + contentSource.environment;
            var route = '#/reindex/' + contentSource.id + '/environment/' + contentSource.environment
            var title = contentSource.appName + ' [' + contentSource.environment + ']'
            return (
                <MenuItem eventKey={itemKey} key={itemKey} href={route}>{title}</MenuItem>
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
                    </Nav>
                </Navbar.Collapse>
            </Navbar>
        );
    }
}