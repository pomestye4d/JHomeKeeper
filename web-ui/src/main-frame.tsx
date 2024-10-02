/* eslint-disable */
// eslint-disable-next-line no-use-before-define
import React, {FunctionComponent, useState} from 'react';
import {ProLayout} from '@ant-design/pro-layout';
import {BrowserRouter, Link, Outlet, Route, Routes, useNavigate} from "react-router-dom";
import './style.css';
import Chart from "./test";

export type MenuItem = {
    name: string,
    id?: string,
    type: string,
    children?: MenuItem[]
}

export type MainFrameProps = {
    // eslint-disable-next-line no-unused-vars
    views: Map<string, FunctionComponent>
    configuration: Map<string, any>,
    menu: MenuItem[]
}

export const ConfigurationContext = React.createContext(new Map());

export function HomeKeeper(props: MainFrameProps) {
    const arr = [] as number[];
    for (let n = 0; n < 1000; n += 1) {
        arr.push(n);
    }
    const [chartData] = useState({
        datasets: [{
            data: [{ x: 10, y: 20 }, { x: 15, y: null }, { x: 20, y: 10 }],
        }],
    });
    return (
        <ConfigurationContext.Provider value={props.configuration}>
            <BrowserRouter>
                <Routes>
                <Route path="/" element={React.createElement(MainFrameComponent, props)}>
                    <Route key="/" path="/" element={<div className="welcome"><Chart/></div>}/>
                    {[...props.views.entries()].map(entry => {
                        return <Route key={entry[0]} path={`${entry[0]}/:id`}
                                      element={React.createElement(entry[1], {})}/>
                    })
                    }
                </Route>
            </Routes>
            </BrowserRouter></ConfigurationContext.Provider>)
}

export function MainFrameComponent(props: MainFrameProps) {
    const createItem = (item: MenuItem) => ({
        name: item.name,
        path: `${item.type}/${item.id}`,
    });
    const navigate = useNavigate();
    return (
        <div
            id="test-pro-layout"
            style={{
                height: 'calc(100vh - 40px)',
                overflow: 'auto',
            }}
        >
            <ProLayout
                title="Home Keeper"
                onMenuHeaderClick={() => {
                    navigate("/");
                }}
                style={{
                    height: '100%',
                }}
                menu={{
                    collapsedShowGroupTitle: true,
                }}
                location={{
                    pathname: "welcome",
                }}
                route={
                    {
                        children: props.menu.map(it => {
                            if ((it.children?.length ?? 0) > 0) {
                                return {
                                    name: it.name,
                                    key: it.name,
                                    children: it.children!!.map(ch => createItem(ch))
                                }
                            }
                            return createItem(it)
                        })
                    }
                }
                openKeys={props.menu.filter(it => (it.children?.length ?? 0) > 0).map(it => it.name)}
                menuItemRender={(item, dom) => (
                    <div
                    >
                        <Link to={item.path || '/'}>{item.name}</Link>
                    </div>
                )}
            >
                <Outlet/>
            </ProLayout>
        </div>
    );
}
