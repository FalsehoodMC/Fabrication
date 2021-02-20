package com.unascribed.fabrication;

public enum Cardinal {
	NORTH( 0,-1),
	EAST ( 1, 0),
	SOUTH( 0, 1),
	WEST (-1, 0);

	private int x;
	private int y;

	Cardinal(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public int xOfs() { return x; }
	public int yOfs() { return y; }

	public Cardinal cw() {
		switch(this) {
		case NORTH: return EAST;
		case EAST: return SOUTH;
		case SOUTH: return WEST;
		default:
		case WEST: return NORTH;
		}
	}

	public Cardinal ccw() {
		switch(this) {
		case NORTH: return WEST;
		case EAST: return NORTH;
		case SOUTH: return EAST;
		default:
		case WEST: return SOUTH;
		}
	}
}