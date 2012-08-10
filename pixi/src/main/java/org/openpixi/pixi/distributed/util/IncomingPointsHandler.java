package org.openpixi.pixi.distributed.util;

import org.openpixi.pixi.physics.util.Point;

import java.util.List;

public interface IncomingPointsHandler {
	void handle(List<Point> points);
}
