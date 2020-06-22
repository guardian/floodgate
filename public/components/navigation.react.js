import React from 'react';
import R from 'ramda';
import { Link } from 'react-router';
import { Navbar, Nav, NavItem, NavDropdown, MenuItem } from 'react-bootstrap'

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
                        <NavItem eventKey={2} href="#/bulk">Bulk Reindexer</NavItem>
                        <NavDropdown eventKey={3} title="Content sources" id="nav-content-source-dropdown">
                            {contentSourceNodes}
                        </NavDropdown>
                    </Nav>
                </Navbar.Collapse>
                <Navbar.Header>
                    <Navbar.Brand>WARNING: YOU ARE USING A BRANCH BUILD</Navbar.Brand>
                </Navbar.Header>
            </Navbar>
        );
    }
}