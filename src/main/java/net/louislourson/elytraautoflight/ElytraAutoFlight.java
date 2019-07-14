package net.louislourson.elytraautoflight;

import com.google.gson.Gson;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.keybinding.FabricKeyBinding;
import net.fabricmc.fabric.api.client.keybinding.KeyBindingRegistry;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

public class ElytraAutoFlight implements ModInitializer, net.fabricmc.api.ClientModInitializer {


    public ElytraConfig config;

    public LinkedList<GraphDataPoint> graph;

	private static FabricKeyBinding keyBinding;
    public static ElytraAutoFlight instance;

    private boolean lastPressed = false;

    private MinecraftClient minecraftClient;

    public boolean showHud;
    private boolean autoFlight;

    private Vec3d previousPosition;
    private double currentVelocity;

    public boolean isDescending;
    public boolean pullUp;
    public boolean pullDown;

    static Gson GSON = new Gson();
    File configFile;

    public String[] hudString;

	@Override
	public void onInitialize() {

		keyBinding = FabricKeyBinding.Builder.create(
				new Identifier("elytraautoflight", "toggle"),
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_R,
				"Auto Flight Elytra"
		).build();

		KeyBindingRegistry.INSTANCE.register(keyBinding);

        lastPressed = false;
        graph = new LinkedList<>();

        ClientTickCallback.EVENT.register(e -> { this.onTick(); });

        ElytraAutoFlight.instance = this;

        this.configFile = new File(FabricLoader.getInstance().getConfigDirectory(), "elytraautoflight/config.json");

        loadSettings();
	}

	private void createAndShowSettings()
    {
        ConfigBuilder configBuilder = ConfigBuilder.create().setTitle("text.elytraautoflight.title").setSavingRunnable(() -> {saveSettings();});
        ConfigCategory categoryGui = configBuilder.getOrCreateCategory("text.elytraautoflight.gui");
        ConfigCategory categoryFlightProfile = configBuilder.getOrCreateCategory("text.elytraautoflight.flightprofile");

        ConfigEntryBuilder entryBuilder = ConfigEntryBuilder.create();

        categoryGui.addEntry(entryBuilder.startIntField("text.elytraautoflight.guiX", config.guiX).setDefaultValue(config.guiX).setSaveConsumer((x) -> config.guiX = x).build());
        categoryGui.addEntry(entryBuilder.startIntField("text.elytraautoflight.guiY", config.guiY).setDefaultValue(config.guiY).setSaveConsumer((x) -> config.guiY = x).build());
        categoryGui.addEntry(entryBuilder.startIntField("text.elytraautoflight.guiWidth", config.guiWidth).setDefaultValue(config.guiWidth).setSaveConsumer((x) -> config.guiWidth = x).build());
        categoryGui.addEntry(entryBuilder.startIntField("text.elytraautoflight.guiHeight", config.guiHeight).setDefaultValue(config.guiHeight).setSaveConsumer((x) -> config.guiHeight = x).build());
        categoryGui.addEntry(entryBuilder.startIntField("text.elytraautoflight.guiSpan", config.guiGraphRealWidth).setDefaultValue(config.guiGraphRealWidth).setSaveConsumer((x) -> config.guiGraphRealWidth = x).build());
        categoryGui.addEntry(entryBuilder.startBooleanToggle("text.elytraautoflight.displayGraph", config.showGraph).setDefaultValue(config.showGraph).setSaveConsumer((x) -> config.showGraph = x).build());

        categoryFlightProfile.addEntry(entryBuilder.startDoubleField("text.elytraautoflight.pullUpAngle", config.pullUpAngle).setDefaultValue(config.pullUpAngle).setSaveConsumer((x) -> config.pullUpAngle = x).build());
        categoryFlightProfile.addEntry(entryBuilder.startDoubleField("text.elytraautoflight.pullDownAngle", config.pullDownAngle).setDefaultValue(config.pullDownAngle).setSaveConsumer((x) -> config.pullDownAngle = x).build());
        categoryFlightProfile.addEntry(entryBuilder.startDoubleField("text.elytraautoflight.pullUpMinVelocity", config.pullUpMinVelocity).setDefaultValue(config.pullUpMinVelocity).setSaveConsumer((x) -> config.pullUpMinVelocity = x).build());
        categoryFlightProfile.addEntry(entryBuilder.startDoubleField("text.elytraautoflight.pullDownMaxVelocity", config.pullDownMaxVelocity).setDefaultValue(config.pullDownMaxVelocity).setSaveConsumer((x) -> config.pullDownMaxVelocity = x).build());
        categoryFlightProfile.addEntry(entryBuilder.startDoubleField("text.elytraautoflight.pullUpSpeed", config.pullUpSpeed).setDefaultValue(config.pullUpSpeed).setSaveConsumer((x) -> config.pullUpSpeed = x).build());
        categoryFlightProfile.addEntry(entryBuilder.startDoubleField("text.elytraautoflight.pullDownSpeed", config.pullDownSpeed).setDefaultValue(config.pullDownSpeed).setSaveConsumer((x) -> config.pullDownSpeed = x).build());


        minecraftClient.openScreen(configBuilder.build());
    }

    private void saveSettings()
    {
        Gson gson = new Gson();
        String configString = gson.toJson(config);
        System.out.println(configString);

    }

    private void loadSettings() {

	    config = new ElytraConfig();

        configFile.getParentFile().mkdirs();

        /*
        if (!configFile.exists() && !configFile.createNewFile()) {
            System.out.println("[elytraautoflight] Failed to save config! Overwriting with default config.");
            config = new ElytraConfig();
            return;
        }

         */

        try {

            //String result = JANKSON.toJson(config).toJson(true, true, 0);
            if (!configFile.exists())
                configFile.createNewFile();
            //FileOutputStream out = new FileOutputStream(configFile, false);

            //out.write(result.getBytes());
            //out.flush();
            //out.close();
        } catch (Exception e) {
            e.printStackTrace();
            //RoughlyEnoughItemsCore.LOGGER.error("[REI] Failed to save config! Overwriting with default config.");
            //config = new ConfigObject();
            return;
        }

    }

	private void onTick() {

        if (minecraftClient == null) minecraftClient = MinecraftClient.getInstance();
        if (config == null) loadSettings();

        if (minecraftClient.player != null) {

            if (minecraftClient.player.isFallFlying())
                showHud = true;
            else {
                showHud = false;
                autoFlight = false;
            }
        }

        if(!lastPressed && keyBinding.isPressed()) {

            if (minecraftClient.player != null) {
                if (minecraftClient.player.isFallFlying()) {
                    // If the player is flying an elytra, we start the auto flight
                    autoFlight = !autoFlight;
                    if (autoFlight) isDescending = true;
                }
                else {
                    // Otherwise we open the settings
                    createAndShowSettings();
                }
            }
        }
        lastPressed = keyBinding.isPressed();





        if (autoFlight) {

            if (isDescending)
            {
                pullUp = false;
                pullDown = true;
                if (currentVelocity >= config.pullDownMaxVelocity) {
                    isDescending = false;
                    pullDown = false;
                    pullUp = true;
                }
            }
            else
            {
                pullUp = true;
                pullDown = false;
                if (currentVelocity <= config.pullUpMinVelocity) {
                    isDescending = true;
                    pullDown = true;
                    pullUp = false;
                }
            }

            if (pullUp) {
                minecraftClient.player.pitch -= config.pullUpSpeed;

                if (minecraftClient.player.pitch <= config.pullUpAngle) minecraftClient.player.pitch = (float)config.pullUpAngle;
            }

            if (pullDown) {
                minecraftClient.player.pitch += config.pullDownSpeed;

                if (minecraftClient.player.pitch >= config.pullDownAngle) minecraftClient.player.pitch = (float)config.pullDownAngle;
            }
        }
        else
        {
            pullUp = false;
            pullDown = false;
        }


        if (showHud) {
            // TODO only if flying?
            computeVelocity();

            double altitude = minecraftClient.player.getPos().y;

            if (hudString == null) hudString = new String[3];

            hudString[0] = "Auto flight : " + (autoFlight ? "Enabled" : "Disabled");
            hudString[1] = "Altitude : " + String.format("%.2f", altitude);
            hudString[2] = "Speed : " + String.format("%.2f", currentVelocity * 20) + " m/s";

            GraphDataPoint newDataPoint;
            if (graph.size() > 0) newDataPoint = new GraphDataPoint(minecraftClient.player.getPos(), graph.getLast().realPosition);
            else newDataPoint = new GraphDataPoint(minecraftClient.player.getPos());
            newDataPoint.pullUp = pullUp;
            newDataPoint.pullDown = pullDown;
            newDataPoint.velocity = currentVelocity;

            addLastDataPoint(newDataPoint);
        }
        else clearGraph();


    }

    private double totalHorizontalDelta = 0;
    private void addLastDataPoint(GraphDataPoint p) {
        graph.addLast(p);
        totalHorizontalDelta += p.horizontalDelta;

        while (totalHorizontalDelta > config.guiGraphRealWidth) removeFirstDataPoint();
    }

    private void removeFirstDataPoint() {
        graph.removeFirst();
        totalHorizontalDelta -= graph.getFirst().horizontalDelta;
    }

    private void clearGraph(){
        graph.clear();
        totalHorizontalDelta = 0;
    }

    private void computeVelocity()
    {
        Vec3d newPosition = minecraftClient.player.getPos();

        if (previousPosition == null)
            previousPosition = newPosition;

        Vec3d difference = new Vec3d(newPosition.x - previousPosition.x, newPosition.y - previousPosition.y, newPosition.z - previousPosition.z);

        previousPosition = newPosition;

        currentVelocity = difference.length();
    }

	@Override
	public void onInitializeClient() {
		System.out.println("Hello Fabric world client!");
	}
}
