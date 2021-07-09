package com.cauldronjs.paper.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.graalvm.polyglot.Value;
import org.jetbrains.annotations.NotNull;

public class SpigotCommandProvider {
  public Command createCommand(String name, Value handler) {
    return new Command(name) {
      @Override
      public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        return handler.execute(sender, commandLabel, args).asBoolean();
      }
    };
  }
}
