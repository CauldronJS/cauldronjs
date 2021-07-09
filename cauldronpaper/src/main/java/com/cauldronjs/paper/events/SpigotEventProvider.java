package com.cauldronjs.paper.events;

import com.cauldronjs.paper.PaperCauldron;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.graalvm.polyglot.Value;

public class SpigotEventProvider {
  final PaperCauldron cauldron;

  public SpigotEventProvider(PaperCauldron cauldron) {
    this.cauldron = cauldron;
  }

  public void registerNewEventHandler(String type, Value handler) throws ClassNotFoundException {
    this.registerNewEventHandler(Class.forName(type).asSubclass(Event.class), handler);
  }

  public void registerNewEventHandler(Class<? extends Event> type, Value handler) throws ClassNotFoundException {
    var listener = new Listener() {
      public int hashCode() {
        return super.hashCode();
      }
    };

    var executor = (EventExecutor) (ignore, event) -> {
      if (event.isAsynchronous()) {
        // queue
        var currentThreadsIsolate = this.cauldron.getIsolateManager().getCurrentThreadIsolate();
        var safeHandler = currentThreadsIsolate.getContext().asValue(handler);
        safeHandler.execute(event);
      } else {
        handler.execute(event);
      }
    };
    this.cauldron.getServer().getPluginManager().registerEvent(
      type,
      listener,
      EventPriority.NORMAL,
      executor,
      this.cauldron
    );
  }
}
