// eslint-disable-next-line no-use-before-define
import React from 'react';
import './chart.css';
import { ReactChart } from 'chartjs-react';
import 'chartjs-adapter-moment';
import moment from 'moment/moment';

export default function Chart() {
  const data = [];
  const time = moment();
  for (let n = 0; n < 100; n += 1) {
    data.push({ x: time.format('YYYY-MM-DD HH:mm:ss'), y: n });
    time.add(600, 'second');
  }
  return (
    <ReactChart
      id="unique-chart-id"
      type="line"
      data={{
        datasets: [{
          label: 'Hello',
          data: data as any,
        }],
      }}
      options={
        {
          scales: {
            x: {
              type: 'time',
              time: {
                displayFormats: {
                  minute: 'DD HH:mm:ss',
                },
              },
              ticks: { source: 'auto' },
            },
            y: {
              type: 'linear',
            },
          },
        }
      }
    />
  );
}
