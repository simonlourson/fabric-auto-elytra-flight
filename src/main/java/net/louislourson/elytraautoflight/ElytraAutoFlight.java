package net.louislourson.elytraautoflight;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.keybinding.FabricKeyBinding;
import net.fabricmc.fabric.api.client.keybinding.KeyBindingRegistry;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.mixin.resources.MixinKeyedResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.util.LinkedList;

public class ElytraAutoFlight implements ModInitializer, net.fabricmc.api.ClientModInitializer {


    public int guiX = 5;
    public int guiY = 5;
    public int guiWidth = 200;
    public int guiHeight = 150;
    public int guiGraphDotWidth = 1;
    public int guiGraphRealWidth = 5000;

    public LinkedList<GraphDataPoint> graph;

	private static FabricKeyBinding keyBinding;
    public static ElytraAutoFlight instance;

    private boolean lastPressed = false;

    private MinecraftClient minecraftClient;

    public boolean showHud;
    private boolean autoFlight;

    private Vec3d previousPosition;
    private double currentVelocity;

    private boolean isDescending;
    private boolean pullUp;
    private boolean pullDown;

    public String hudString;

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		System.out.println("Hello Fabric world!");

		keyBinding = FabricKeyBinding.Builder.create(
				new Identifier("tutorial", "spook"),
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_R,
				"Wiki Keybinds"
		).build();

		KeyBindingRegistry.INSTANCE.register(keyBinding);

        lastPressed = false;
        graph = new LinkedList<>();

		ClientTickCallback.EVENT.register(e -> { this.onTick(); });

        ElytraAutoFlight.instance = this;
	}

	private void onTick()
    {

        if (minecraftClient == null) minecraftClient = MinecraftClient.getInstance();

        if (minecraftClient.player != null && minecraftClient.player.isFallFlying()) {
            showHud = true;

            if(!lastPressed && keyBinding.isPressed()) {
                System.out.println("autoflight go!");
                autoFlight = !autoFlight;

                if (autoFlight) isDescending = true;
            }

            if (keyBinding.isPressed()) lastPressed = true;
            else lastPressed = false;


            // TODO Detect keypress here
        }
        else {
            showHud = false;
            autoFlight = false;
        }

        if (showHud) {
            // TODO only if flying?
            computeVelocity();

            double altitude = minecraftClient.player.getPos().y;

            hudString = "Auto flight : " + autoFlight;
            hudString += " Altitude : " + String.format("%.2f", altitude);
            hudString += " Velocity : " + String.format("%.2f", currentVelocity);
            if (autoFlight) hudString += (isDescending ? " : Gaining speed" : " : Gaining altitude");

            GraphDataPoint newDataPoint;
            if (graph.size() > 0) newDataPoint = new GraphDataPoint(minecraftClient.player.getPos(), graph.getLast().realPosition);
            else newDataPoint = new GraphDataPoint(minecraftClient.player.getPos());

            addLastDataPoint(newDataPoint);
        }
        else clearGraph();

        double pullUpAngle = -46.633514;
        double pullDownAngle = 37.19872;
        double pullUpMinVelocity = 1.9102669;
        double pullDownMaxVelocity = 2.3250866;
        double pullUpSpeed = 2.1605124 * 3;
        double pullDownSpeed = 0.20545267 * 3;


        if (autoFlight) {

            if (isDescending)
            {
                pullUp = false;
                pullDown = true;
                if (currentVelocity >= pullDownMaxVelocity) {
                    isDescending = false;
                    pullDown = false;
                    pullUp = true;
                }
            }
            else
            {
                pullUp = true;
                pullDown = false;
                if (currentVelocity <= pullUpMinVelocity) {
                    isDescending = true;
                    pullDown = true;
                    pullUp = false;
                }
            }

            if (pullUp) {
                minecraftClient.player.pitch -= pullUpSpeed;

                if (minecraftClient.player.pitch <= pullUpAngle) minecraftClient.player.pitch = (float)pullUpAngle;
            }

            if (pullDown) {
                minecraftClient.player.pitch += pullDownSpeed;

                if (minecraftClient.player.pitch >= pullDownAngle) minecraftClient.player.pitch = (float)pullDownAngle;
            }
        }


    }

    private double totalHorizontalDelta = 0;
    private void addLastDataPoint(GraphDataPoint p) {
        graph.addLast(p);
        totalHorizontalDelta += p.horizontalDelta;

        while (totalHorizontalDelta > guiGraphRealWidth) removeFirstDataPoint();
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
