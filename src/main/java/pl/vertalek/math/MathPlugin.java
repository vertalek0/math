package pl.vertalek.math;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiFunction;

public class MathPlugin extends JavaPlugin implements Listener {

    private final ThreadLocalRandom RANDOM = ThreadLocalRandom.current();
    private final int TIME = getConfig().getInt("time");
    private final int MIN_NUMBER = getConfig().getInt("minNumber");
    private final int MAX_NUMBER = getConfig().getInt("maxNumber");
    private final String CREATE_MATH = getConfig().getString("createMath");
    private final String SOLVED_MATH = getConfig().getString("solvedMath");
    private int RESULT = 0;

    @Override
    public void onEnable() {
        createMath();
        getServer().getPluginManager().registerEvents(this, this);
    }

    private void createMath() {
        getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            int randomMathTypeId = RANDOM.nextInt(MathType.values().length);
            for (MathType mathType : MathType.values()) {
                if (mathType.id != randomMathTypeId) return;
                int numberOne = RANDOM.nextInt(MIN_NUMBER, MAX_NUMBER);
                int numberTwo = RANDOM.nextInt(MIN_NUMBER, MAX_NUMBER);
                RESULT = mathType.biFunction.apply(numberOne, numberTwo);
                Bukkit.broadcast(colored(getConfig().getString(CREATE_MATH
                        .replace("%numberOne%", String.valueOf(numberOne))
                        .replace("%numberTwo%", String.valueOf(numberTwo))
                        .replace("%operation%", mathType.operation))));
            }

        }, 0L, TIME * 20L);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void handle(AsyncChatEvent event) {
        Component message = event.message();
        if (message.contains(Component.text(0))) return;
        if (!message.contains(Component.text(RESULT))) return;
        Bukkit.broadcast(colored(SOLVED_MATH
                .replace("%player%", event.getPlayer().getName())
                .replace("%result%", String.valueOf(RESULT))));
    }

    private Component colored(String text) {
        return Component.text(ChatColor.translateAlternateColorCodes('&', text));
    }

    private enum MathType {
        ADDITION(1, Integer::sum, "+"),
        SUBTRACT(2, (a, b) -> a - b, "-"),
        MULTIPLICATION(3, (a, b) -> a * b, "*");

        private final int id;
        private final BiFunction<Integer, Integer, Integer> biFunction;
        private final String operation;

        MathType(int id, BiFunction<Integer, Integer, Integer> biFunction, String operation) {
            this.id = id;
            this.biFunction = biFunction;
            this.operation = operation;
        }
    }
}