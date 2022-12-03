//package com.i4rt.temperaturecontrol.tasks;
//
//
//import java.util.Calendar;
//import java.util.Date;
//import java.util.Timer;
//import java.util.TimerTask;
//
//public class Executor
//{
//    private final Runnable dailyTask;
//    private final int hour;
//    private final int minute;
//    private final int second;
//    private final String runThreadName;
//
//    public Executor(Integer hour, Integer minute, Integer second, Runnable dailyTask, String runThreadName)
//    {
//        this.dailyTask = dailyTask;
//        this.hour = hour;
//        this.minute = minute;
//        this.second = second;
//        this.runThreadName = runThreadName;
//    }
//
//    public void start()
//    {
//        startTimer();
//    }
//
//    private void startTimer() {
//        new Timer(runThreadName, true).schedule(new TimerTask()
//        {
//            @Override
//            public void run()
//            {
//                System.out.println("started!");
//                dailyTask.run();
//                startTimer();
//            }
//        }, getNextRunTime());
//    }
//
//
//    private Date getNextRunTime()
//    {
//        Calendar startTime = Calendar.getInstance();
//        Calendar now = Calendar.getInstance();
//        startTime.set(Calendar.HOUR_OF_DAY, hour);
//        startTime.set(Calendar.MINUTE, minute);
//        startTime.set(Calendar.SECOND, second);
//        startTime.set(Calendar.MILLISECOND, 0);
//
//        if(startTime.before(now) || startTime.equals(now))
//        {
//            startTime.add(Calendar.DATE, 1);
//        }
//        System.out.println("time to start " + startTime.getTime());
//        return startTime.getTime();
//    }
//}