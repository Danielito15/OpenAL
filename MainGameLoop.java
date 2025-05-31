package engineTester;

import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector3f;

import Audio.AudioMaster;
import Audio.Source;
import control.Circunferencia2D;
import control.OrbitController;
import control.RelativeOrbitController;
import entities.Camera;
import entities.Entity;
import entities.Light;
import models.TexturedModel;
import renderEngine.DisplayManager;
import renderEngine.Loader;
import renderEngine.MasterRenderer;
import renderEngine.RawModel;
import renderEngine.Renderer;
import shaders.StaticShader;
import shaders.SunShader;
import toolbox.Maths;
import toolbox.PlanetFactory;

public class MainGameLoop {

	public static void main(String[] args) {
		boolean orbitaActiva = true, pPresionada = false;

        DisplayManager.createDisplay();
        Loader loader = new Loader();
        StaticShader shader = new StaticShader();
        SunShader sunShader = new SunShader();
        MasterRenderer masterRenderer = new MasterRenderer();
        Renderer renderer = new Renderer(shader);
        
        
        //Prueba de sonido--------------------------------------------------
		AudioMaster.init();
		AL10.alDistanceModel(AL11.AL_LINEAR_DISTANCE_CLAMPED);
		AudioMaster.setListenerData(0,0,0);
		
		int buffer = AudioMaster.loadSound("Audio/sol.wav");
		Source source = new Source(1f,0.2f,0.5f);
		source.setLooping(true);
		source.play(buffer);
		//-------------------------------------------------------------------

        sunShader.start();
        sunShader.loadProjectionMatrix(renderer.getProjectionMatrix());
        sunShader.stop();

        // --- Modelos base ---
        RawModel model = PlanetFactory.updateModel(loader, 100, 100);
        RawModel modelCube = PlanetFactory.createTexturedCubeModel(loader, 0.5f);

        // --- Creación de modelos usando la fábrica ---
        TexturedModel texturedStar   = PlanetFactory.createPlanetModel(loader, modelCube, "estrellas_binarizadas", 20, 0.1f);
        TexturedModel texturedSol    = PlanetFactory.createPlanetModel(loader, model, "sol", 10, 0);
        TexturedModel texturedMerc   = PlanetFactory.createPlanetModel(loader, model, "mercurio", 20, 0.2f);
        TexturedModel texturedVenus  = PlanetFactory.createPlanetModel(loader, model, "venus", 20, 0.2f);
        TexturedModel texturedTierra = PlanetFactory.createPlanetModel(loader, model, "tierra", 20, 0.2f);
        TexturedModel texturedMarte  = PlanetFactory.createPlanetModel(loader, model, "marte", 20, 0.2f);
        TexturedModel texturedLuna   = PlanetFactory.createPlanetModel(loader, model, "luna", 20, 0.2f);
        TexturedModel texturedFobos  = PlanetFactory.createPlanetModel(loader, model, "fobos", 20, 0.2f);
        TexturedModel texturedDeimos = PlanetFactory.createPlanetModel(loader, model, "deimos", 20, 0.2f);

        // --- Entidades ---
        Entity star  	= PlanetFactory.createEntity(texturedStar, 0, 0, 0, PlanetFactory.RADIOS.get("star"));
        Entity sol    	= PlanetFactory.createEntity(texturedSol, 0, 0, 0, PlanetFactory.RADIOS.get("sol"));
        Entity mercurio = PlanetFactory.createEntity(texturedMerc, 0.387f, 0, 0, PlanetFactory.RADIOS.get("mercurio"));
        Entity venus    = PlanetFactory.createEntity(texturedVenus, 0.723f, 0, 0, PlanetFactory.RADIOS.get("venus"));
        Entity tierra   = PlanetFactory.createEntity(texturedTierra, 1.0f, 0, 0, PlanetFactory.RADIOS.get("tierra"));
        Entity marte    = PlanetFactory.createEntity(texturedMarte, 1.524f, 0, 0, PlanetFactory.RADIOS.get("marte"));
        Entity luna     = PlanetFactory.createEntity(texturedLuna, 0.387f, 0, 0, PlanetFactory.RADIOS.get("luna"));
        Entity fobos    = PlanetFactory.createEntity(texturedFobos, 0.387f, 0, 0, PlanetFactory.RADIOS.get("fobos"));
        Entity deimos   = PlanetFactory.createEntity(texturedDeimos, 0.387f, 0, 0, PlanetFactory.RADIOS.get("deimos"));

        // --- Controladores de órbitas ---
        OrbitController[] orbitControllers = {
            new OrbitController(mercurio, Circunferencia2D.generarCircunferencia(0.387f, 0.0159f), 0f, true),
            new OrbitController(venus,    Circunferencia2D.generarCircunferencia(0.723f, 0.0109f), 0f, true),
            new OrbitController(tierra,   Circunferencia2D.generarCircunferencia(1.0f,   0.011f),  0f, true),
            new OrbitController(marte,    Circunferencia2D.generarCircunferencia(1.524f, 0.0081f), 0f, true)
        };

        RelativeOrbitController[] relativeControllers = {
            new RelativeOrbitController(luna, tierra, 0.08f, 0.02f, 0.025f, true),
            new RelativeOrbitController(fobos, marte, 0.04f, 0.015f, 0f, true),
            new RelativeOrbitController(deimos, marte, 0.2f, 0.01f, 0f, true)
        };

        // --- Cámara y luz ---
        Camera camera = new Camera();
        Light sunLight = new Light(new Vector3f(0, 0, 0), new Vector3f(1.0f, 0.95f, 0.85f));

        // --- Arreglos auxiliares ---
        Entity[] rotables = { sol, star, mercurio, venus, tierra, marte, luna, fobos, deimos };
        float[] velocidades = { 0.04f, 0.005f, 0.04f, -0.004f, 1f, 1.03f, 0.4f, 0.4f, 0.6f };
        List<Entity> planetas = List.of(mercurio, venus, tierra, marte, luna, fobos, deimos);
	    
		while (!Display.isCloseRequested()) {
			
			Vector3f camPos = camera.getPosition();
			AudioMaster.setListenerData(camPos.x, camPos.y, camPos.z);
			
//			Vector3f eartPos = tierra.getPosition();
//			source.setPosition(eartPos.x, eartPos.y, eartPos.z);

		    //Rotación de cuerpos
		    for (int i = 0; i < rotables.length; i++) {
		        rotables[i].increaseRotation(0, velocidades[i], 0);
		    }

		    //Pausar/continuar órbitas con 'P'
		    if (Keyboard.isKeyDown(Keyboard.KEY_P)) {
		        if (!pPresionada) {
		            orbitaActiva = !orbitaActiva;
		            pPresionada = true;
		        }
		    } else {
		        pPresionada = false;
		    }

		    //Actualización de órbitas
		    if (orbitaActiva) {
		        for (OrbitController controller : orbitControllers) controller.update();
		    }
		    for (RelativeOrbitController rc : relativeControllers) rc.update();

		    camera.move();
		    masterRenderer.render(sunLight, camera);

		    //Render Sol y estrellas con emisión personalizada
		    sunShader.start();
		    sunShader.loadViewMatrix(camera);

		    renderAutoEmission(sunShader, renderer, sol, new Vector3f(2.5f, 1.8f, 1.5f)); // Sol
		    renderAutoEmission(sunShader, renderer, star, new Vector3f(0.5f, 0.5f, 0.5f)); // Fondo estelar

		    sunShader.stop();

		    //Registrar planetas para batch rendering
		    for (Entity planet : planetas) {
		        masterRenderer.processEntity(planet);
		    }

		    DisplayManager.updateDisplay();
		}

		source.delate();
	    masterRenderer.cleanUp();  // libera shader de planetas
	    sunShader.cleanUp();       // libera shader del sol
	    loader.cleanUp();          // libera VAOs y VBOs
	    AudioMaster.cleanUp();
	    DisplayManager.closeDisplay();
	}
    
    private static void renderAutoEmission(SunShader shader, Renderer renderer, Entity entity, Vector3f emission) {
        shader.loadEmissionColor(emission);
        shader.loadTransformationMatrix(
            Maths.createTransformationMatrix(
                entity.getPosition(),
                entity.getRotX(), entity.getRotY(), entity.getRotZ(),
                entity.getScale()
            )
        );
        renderer.renderSun(entity, shader);
    }

}
