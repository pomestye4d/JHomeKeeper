// eslint-disable-next-line no-use-before-define
import React from 'react';
import { Button as AntdButton } from 'antd/lib';

type ButtonData = {
  id:string,
  name: string,
}

async function processButtonClick(id:string) {
  const result = await ((await fetch('/ui/button', {
    method: 'POST',
    body: JSON.stringify({
      id,
    }),
  })).json()) as {result: string};
  if (result.result === 'ERROR') {
    console.log('error');
  }
}
export default function Button(props:ButtonData) {
  const { id, name } = props;
  return (

    <AntdButton
      id={id}
      onClick={async () => {
        await processButtonClick(id);
      }}
    >
      {name}
    </AntdButton>
  );
}
