package com.haeram.tools.com.statistic;

import com.haeram.tools.com.debug.Tracer;

import java.util.concurrent.ConcurrentLinkedQueue;

public final class TimeAvg {
    private static final String TAG = "TimeAvg";

    private static final Object WAIT_SYNC_OBJ = new Object();
    private static final Object WORKER_SYNC_OBJ = new Object();
    private static Average mAvg = null;
    private static ConcurrentLinkedQueue<Long> mQueue = new ConcurrentLinkedQueue<Long>();
    private static boolean mWorkStatus = false;
    private TimeAvgListener mTimeAvgListener;

    public TimeAvg() {
        mAvg = new Average();
    }

    public void setListener(TimeAvgListener listener) {
        mTimeAvgListener = listener;
    }

    public void update(long elapsedTime) {
        try {
            mQueue.offer(Long.valueOf(elapsedTime));
        } catch (NullPointerException e) {
            return;
        }
        Tracer.d(TAG, String.format("Last ElapsedTime[%d]", elapsedTime));

        enableCalculate();
    }


    public void clear() {
        mAvg.reset();
    }

    private void enableCalculate() {
        synchronized (WAIT_SYNC_OBJ) {
            WAIT_SYNC_OBJ.notifyAll();
        }

        synchronized (WORKER_SYNC_OBJ) {
            if (!mWorkStatus) {
                mWorkStatus = true;
                new Thread(new AvgThread()).start();
            }
        }
    }

    class AvgThread implements Runnable {
        @Override
        public void run() {
            while (!mQueue.isEmpty()) {
                Long lval = (Long) mQueue.poll();
                if (lval != null) {
                    mAvg.calculate(lval.longValue());
                }

                if (mQueue.isEmpty()) {
                    //ToDo: print trace;
                    Tracer.d(TAG, String.format("RTT statistic - numCount[%d] msecAverge[%d]",
                            mAvg.getCount(), mAvg.getAverage()));

                    if (mTimeAvgListener != null) {
                        mTimeAvgListener.onCalculated(mAvg.getCount(), mAvg.getAverage());
                    }

                    synchronized (WAIT_SYNC_OBJ) {
                        try {
                            WAIT_SYNC_OBJ.wait(10000);
                        } catch (InterruptedException ie) {
                        }
                    }

                    synchronized (WORKER_SYNC_OBJ) {
                        if (mQueue.isEmpty()) {
                            mWorkStatus = false;
                            break;
                        }
                    }
                }
            }
        }
    }

    class Average {
        private final long MAX_COUNT = 10000;
        private long mCount = 0;
        private long mAccumVal = 0;
        private long mTotalAvg = 0;

        synchronized void calculate(long val) {
            if (mCount >= MAX_COUNT) {
                reset();
                Tracer.w(TAG, "reset average due to over MAX_TIMES");
            }
            mAccumVal += val;
            mTotalAvg = mAccumVal / ++mCount;
        }

        synchronized long getAverage() {
            return mTotalAvg;
        }

        synchronized long getCount() {
            return mCount;
        }

        private synchronized void reset() {
            mCount = 0;
            mAccumVal = 0;
            mTotalAvg = 0;
            Tracer.w(TAG, "average reset completed");
        }
    }
}