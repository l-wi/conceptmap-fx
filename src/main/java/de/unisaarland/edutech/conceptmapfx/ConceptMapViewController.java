package de.unisaarland.edutech.conceptmapfx;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisaarland.edutech.conceptmapfx.InputViewController.Position;
import de.unisaarland.edutech.conceptmapfx.event.ConceptDeletedListener;
import de.unisaarland.edutech.conceptmapfx.event.LinkDeletedListener;
import de.unisaarland.edutech.conceptmapfx.event.NewConceptListener;
import de.unisaarland.edutech.conceptmapfx.event.NewLinkListener;
import de.unisaarland.edutech.conceptmapping.CollaborativeString;
import de.unisaarland.edutech.conceptmapping.Concept;
import de.unisaarland.edutech.conceptmapping.ConceptMap;
import de.unisaarland.edutech.conceptmapping.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

public class ConceptMapViewController
		implements NewLinkListener, NewConceptListener, LinkDeletedListener, ConceptDeletedListener {

	 private static final Logger LOG = LoggerFactory.getLogger(ConceptMapViewController.class);

	private List<ConceptDeletedListener> conceptDeletedListners = new ArrayList<ConceptDeletedListener>();
	private List<LinkDeletedListener> linkDeletedListeners = new ArrayList<LinkDeletedListener>();

	@FXML
	private AnchorPane conceptMapPane;

	private ConceptMap conceptMap;
	private List<InputViewController> inputControllers = new ArrayList<InputViewController>();

	public void addConceptDeletedListener(ConceptDeletedListener l) {
		conceptDeletedListners.add(l);
	}

	public void addLinkDeletedListener(LinkDeletedListener l) {
		linkDeletedListeners.add(l);
	}

	public void newLink(ConceptViewController cv1, ConceptViewController c2, User u) {
		// TODO Auto-generated method stub

	}

	public void newConcept(InputViewController inputViewController) {
		try {
			// TODO before doing so test that there is no other empty concept
			// for user
			User owner = inputViewController.getUser();
			Concept concept = new Concept(new CollaborativeString(owner));
			conceptMap.addConcept(concept);

			FXMLLoader loader = new FXMLLoader(getClass().getResource("ConceptView.fxml"));
			Pane conceptViewPane = loader.load();

			ConceptViewController conceptViewController = loader.getController();

			for (InputViewController i : inputControllers) {
				conceptViewController.addConceptEditRequestedListener(i);
				conceptViewController.addNewLinkListener(this);
			}

			conceptViewController.setConcept(concept);
			conceptViewController.setParticipants(conceptMap.getExperiment().getParticipants());

			// TODO what if current space is occupied
			conceptViewPane.boundsInLocalProperty().addListener((c, n, o) -> {

				Point2D p = new Point2D(0, -50);
				Point2D pScene = inputViewController.transformLocalToScene(p);

				double x = pScene.getX();
				double y = pScene.getY();

				if (inputViewController.getPosition() == Position.NORTH) {
					x = x - n.getWidth();
				} else if (inputViewController.getPosition() == Position.WEST) {
					x = x - n.getWidth() / 2 + n.getHeight() / 2;
					y = y + n.getWidth() / 2 - n.getHeight() / 2;
				} else if (inputViewController.getPosition() == Position.EAST) {
					x = x - n.getWidth() / 2 - n.getHeight() / 2;
					y = y - n.getWidth() / 2 - n.getHeight() / 2;
				} else if (inputViewController.getPosition() == Position.SOUTH) {
					y = y - n.getHeight();
				}

				conceptViewPane.setTranslateX(x);
				conceptViewPane.setTranslateY(y);

				conceptViewPane.setRotate(inputViewController.getRotate());
			});

			conceptMapPane.getChildren().add(conceptViewPane);

			requestInput(owner, conceptViewController);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw new RuntimeException(e);
		}
	}

	private void requestInput(User owner, ConceptViewController conceptViewController) {
		LOG.info("requesting input for user:\t"+ owner + " on new concept!" );
		conceptViewController.setUserEnabled(owner,true);
	}

	public void linkDeleted(LinkViewController lv, User u) {
		// TODO Auto-generated method stub

	}

	public void conceptDeleted(ConceptViewController cv, User u) {
		// TODO Auto-generated method stub

	}

	public void setConceptMap(ConceptMap conceptMap) {
		this.conceptMap = conceptMap;
	}

	public void addInputViewController(InputViewController inputController) {
		inputControllers.add(inputController);
	}

}
