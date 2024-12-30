// eslint-disable-next-line no-use-before-define
import React from 'react';
import {createRoot} from 'react-dom/client';
import {HomeKeeper, MenuItem} from './src/main-frame';
import {views} from "./src/registry";

type UiElement = {
  id: string;
  name: string;
  type: string;
} & any

type UiGroup = {
  name:string;
  elements: UiElement[];
}
type UiWrapper = {
  groups: UiGroup[];
}

async function start() {
  const ui = await ((await fetch('/ui/config', {
    method: 'GET',
  })).json()) as UiWrapper;
  const menu = ui.groups.map((it) => ({
    name: it.name,
    children: it.elements.map((elm) => ({
      name: elm.name,
      type: elm.type,
      id: elm.id,
    } as MenuItem)),
  } as MenuItem));
  const configuration = new Map();
  ui.groups.forEach((it) => it.elements.forEach((elm) => {
    configuration.set(elm.id, elm);
  }));
  const root = createRoot(document.getElementById('root') as Element);
  root.render(<HomeKeeper menu={menu} views={views} configuration={configuration} />);
}

start();
