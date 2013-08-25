package edgruberman.bukkit.simplelocks.util;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

public abstract class Feedback {

    public static Feedback COMMAND_RESULT_SUCCESS = new Audible(Sound.NOTE_PIANO, 1.0F);

    public static Feedback COMMAND_RESULT_WARNING = new FeedbackSequence(
            new FeedbackSequence.Entry(new Audible(Sound.NOTE_BASS, 1.5F), 3)
            , new FeedbackSequence.Entry(new Audible(Sound.NOTE_BASS, 1.2F))
    );

    public static Feedback COMMAND_RESULT_FAILURE= new Audible(Sound.NOTE_BASS, 1.0F, 1.0F, 2, 0, 3);





    private static Plugin plugin;
    private static BukkitScheduler scheduler;

    public static void register(final Plugin plugin, final BukkitScheduler scheduler) {
        Feedback.plugin = plugin;
        Feedback.scheduler = scheduler;
    }



    private final int count;
    private final long delay;
    private final long period;

    protected Feedback() {
        this(1, 0L, 0L);
    }

    protected Feedback(final int count, final long delay, final long period) {
        this.count = count;
        this.delay = delay;
        this.period = period;
    }

    public int getCount() {
        return this.count;
    }

    public long getDelay() {
        return this.delay;
    }

    public long getPeriod() {
        return this.period;
    }

    public void send(final Player target) {
        if (this.count == 1 && this.delay == 0) {
            this.deliverSend(target);
            return;
        }

        Task.send(this, target);
    }

    public void broadcast(final Player target) {
        if (this.count == 1 && this.delay == 0) {
            this.deliverBroadcast(target);
            return;
        }

        Task.broadcast(this, target);
    }

    protected abstract void deliverSend(final Player target);

    protected abstract void deliverBroadcast(final Player target);





    public static class FeedbackSequence extends Feedback {

        private final List<Entry> entries;

        protected FeedbackSequence(final Entry... entries) {
            this(1, 0L, 0L, entries);
        }

        protected FeedbackSequence(final int count, final long delay, final long period, final Entry... entries) {
            super(count, delay, period);
            this.entries = Arrays.asList(entries);
        }

        public List<Entry> getEntries() {
            return this.entries;
        }

        @Override
        protected void deliverSend(final Player target) {
            final FeedbackSequence.Index index = new FeedbackSequence.Index(this);
            index.deliverSend(target);
        }

        @Override
        protected void deliverBroadcast(final Player target) {
            final FeedbackSequence.Index index = new FeedbackSequence.Index(this);
            index.deliverBroadcast(target);
        }



        public static class Entry {

            private final Feedback feedback;
            private final long pause;

            protected Entry(final Feedback feedback) {
                this(feedback, -1L);
            }

            protected Entry(final Feedback feedback, final long pause) {
                this.feedback = feedback;
                this.pause = pause;
            }

            public Feedback getFeedback() {
                return this.feedback;
            }

            public long getPause() {
                return this.pause;
            }

        }



        public static class Index extends Feedback {

            private final FeedbackSequence sequence;

            private int index = 0;

            protected Index(final FeedbackSequence sequence) {
                this.sequence = sequence;
            }

            @Override
            protected void deliverSend(final Player target) {
                final Entry current = this.sequence.getEntries().get(this.index);
                current.getFeedback().deliverSend(target);
                this.index++;
                if (this.index >= this.sequence.getEntries().size()) return;
                Task.send(this, target, current.getPause());
            }

            @Override
            protected void deliverBroadcast(final Player target) {
                final Entry current = this.sequence.getEntries().get(this.index);
                current.getFeedback().deliverBroadcast(target);
                this.index++;
                if (this.index >= this.sequence.getEntries().size()) return;
                Task.broadcast(this, target, current.getPause());
            }

        }

    }





    public static class Audible extends Feedback {

        private final Sound sound;
        private final float volume;
        private final float pitch;

        public Audible(final Sound sound, final float pitch) {
            this(sound, pitch, 1.0F, 1, 0L, -1L);
        }

        public Audible(final Sound sound, final float pitch, final float volume) {
            this(sound, pitch, volume, 1, 0L, -1L);
        }

        public Audible(final Sound sound, final float pitch, final float volume, final int count, final long delay, final long period) {
            super(count, delay, period);
            this.sound = sound;
            this.pitch = pitch;
            this.volume = volume;
        }

        @Override
        protected void deliverSend(final Player target) {
            target.playSound(target.getLocation(), this.sound, this.volume, this.pitch);
        }

        @Override
        protected void deliverBroadcast(final Player target) {
            target.getWorld().playSound(target.getLocation(), this.sound, this.volume, this.pitch);
        }

    }





    public abstract static class Task implements Runnable {

        public static Task.Send send(final Feedback feedback, final Player target) {
            final Task.Send send = new Task.Send(feedback, target);
            send.run();
            return send;
        }

        public static Task.Send send(final Feedback feedback, final Player target, final long delay) {
            final Task.Send send = new Task.Send(feedback, target, 1, delay, -1L);
            send.run();
            return send;
        }

        public static Task.Broadcast broadcast(final Feedback feedback, final Player target) {
            final Task.Broadcast broadcast = new Task.Broadcast(feedback, target);
            broadcast.run();
            return broadcast;
        }

        public static Task.Broadcast broadcast(final Feedback feedback, final Player target, final long delay) {
            final Task.Broadcast broadcast = new Task.Broadcast(feedback, target, 1, delay, -1L);
            broadcast.run();
            return broadcast;
        }



        protected final Feedback feedback;
        protected final Player target;
        protected final int count;
        protected final long delay;
        protected final long period;

        private int delivered = 0;
        private BukkitTask task = null;

        Task(final Feedback feedback, final Player target) {
            this(feedback, target, feedback.getCount(), feedback.getDelay(), feedback.getPeriod());
        }

        Task(final Feedback feedback, final Player target, final int count, final long delay, final long period) {
            this.feedback = feedback;
            this.target = target;
            this.count = count;
            this.delay = delay;
            this.period = period;
        }

        @Override
        public void run() {
            // cancel task when all delivered
            if (this.task != null && this.delivered >= this.count) {
                this.task.cancel();
                return;
            }

            // deliver if no delay or this is a task running
            if (this.delay <= 0 || this.task != null) {
                this.deliver();
                this.delivered++;
            }

            // schedule task if more need to be delivered and not already
            if (this.task == null && this.delivered < this.count) {
                final long delay = ( this.delay > 0 ? this.delay : this.period );
                this.task = Feedback.scheduler.runTaskTimer(Feedback.plugin, this, delay, this.period);
            }
        }

        abstract void deliver();



        public static class Send extends Task {

            Send(final Feedback feedback, final Player target) {
                super(feedback, target);
            }

            Send(final Feedback feedback, final Player target, final int count, final long delay, final long period) {
                super(feedback, target, count, delay, period);
            }

            @Override
            public void deliver() {
                this.feedback.deliverSend(this.target);
            }

        }



        public static class Broadcast extends Task {

            Broadcast(final Feedback feedback, final Player target) {
                super(feedback, target);
            }

            Broadcast(final Feedback feedback, final Player target, final int count, final long delay, final long period) {
                super(feedback, target, count, delay, period);
            }

            @Override
            public void deliver() {
                this.feedback.deliverBroadcast(this.target);
            }

        }

    }

}
