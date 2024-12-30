// eslint-disable-next-line no-use-before-define
import React from 'react';

type LabelData = {
  id:string,
  name: string,
}

export default function Label(props:LabelData) {
  return (
    // eslint-disable-next-line react/destructuring-assignment
    <div id={props.id}>{props.name}</div>
  );
}
