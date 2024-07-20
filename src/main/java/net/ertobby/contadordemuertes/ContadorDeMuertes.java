package net.ertobby.contadordemuertes;

import com.mojang.logging.LogUtils;
import net.ertobby.contadordemuertes.comandos.reiniciarContador;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.StatType;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.minecraftforge.server.command.ConfigCommand;
import org.apache.logging.log4j.core.jmx.Server;
import org.openjdk.nashorn.internal.runtime.arrays.AnyElements;
import org.slf4j.Logger;

//Command related

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.commands.CommandSource;

//Scoreboard related
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;



//manejo de eventos
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;

import javax.annotation.processing.SupportedSourceVersion;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(ContadorDeMuertes.MOD_ID)
public class ContadorDeMuertes
{
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "contadormuertesdelertobby";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public ContadorDeMuertes()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(this);

        modEventBus.addListener(this::addCreative);

    }

    private void commonSetup(final FMLCommonSetupEvent event) {

    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {

    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {

    }

    public static void mostrarJugadores(ServerPlayer jugador) {
        int contMuertes = 0;

        Scoreboard scoreboard = jugador.getScoreboard();

        Objective objective = scoreboard.getObjective("contadorMuertes");

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();

        if (objective != null) {

            for (ServerPlayer jugadorConectado : server.getPlayerList().getPlayers()) {
                contMuertes = jugadorConectado.getStats().getValue(Stats.CUSTOM.get(Stats.DEATHS));
                scoreboard.getOrCreatePlayerScore(jugadorConectado.getScoreboardName(), objective).setScore(contMuertes); // Ejemplo de puntuación
            }
        }
    }



    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    // Clase para manejar los eventos de Forge
    @Mod.EventBusSubscriber(modid = ContadorDeMuertes.MOD_ID, bus = Bus.FORGE, value = Dist.CLIENT)
    public static class ForgeEvents {


       @SubscribeEvent
       public static void onPlayerLoggedIn(PlayerLoggedInEvent event) {
           if (event.getEntity() instanceof ServerPlayer jugador) {
               // Informamos de la carga del mod al chat del jugador
               jugador.sendSystemMessage(
                       Component.literal("¡El mod " + ContadorDeMuertes.MOD_ID + " ha sido cargado!")
               );

               mostrarJugadores(jugador);
           }
        }

        @SubscribeEvent
        public static void onCommandRegister(RegisterCommandsEvent event) {
            new reiniciarContador(event.getDispatcher());

            ConfigCommand.register(event.getDispatcher());
        }

        @SubscribeEvent
        public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
            if (event.getEntity() instanceof ServerPlayer jugador) {
                mostrarJugadores(jugador);

                //ServerPlayer jugador = (ServerPlayer) event.getEntity();

                /*
                int contMuertes = 0;

                MinecraftServer server = ServerLifecycleHooks.getCurrentServer();


                for (ServerPlayer jugadorConectado : server.getPlayerList().getPlayers()) {
                    contMuertes = jugadorConectado.getStats().getValue(Stats.CUSTOM.get(Stats.DEATHS));

                    //para que muestre las muertes de antes más la de ahora
                    contMuertes++;

                    //mostrar muertes en el log
                    jugadorConectado.sendSystemMessage(
                            Component.literal("El jugador " + jugadorConectado.getName().getString() + " ha muerto " + contMuertes + " veces, puto subnormal")
                    );

                    //mostrar muertes en el ScoreBoard
                    scoreboard.getOrCreatePlayerScore(jugadorConectado.getScoreboardName(), objective).setScore(1); // Ejemplo de puntuación
                }*/
            }
        }

        @SubscribeEvent
        public static void onServerStarting (ServerStartingEvent event) {
            Scoreboard scoreboard = event.getServer().getScoreboard();

            //Comprobamos primero que no existe el objetivo (por ejemplo al cargar un mundo que ya existe)
            boolean existe = false;

            for (Objective objectiveRecorrer : scoreboard.getObjectives()) {
                if (objectiveRecorrer.getName().equals("contadorMuertes")) {
                    existe = true;
                }
            }

            //Si no existe, lo crea
            if (!existe) {
                Objective objective = scoreboard.addObjective(
                        "contadorMuertes",
                        ObjectiveCriteria.DUMMY,
                        Component.literal("MUERTES"),
                        ObjectiveCriteria.RenderType.INTEGER
                );
                scoreboard.setDisplayObjective(1, objective);
            }
        }
    }
}
