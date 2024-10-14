package jvn;

public enum LockStates {
	NL, // no lock
	RC, // Read cached
	WC, // Write cached
	R, // Read
	W, // Write
	RWC // ReadWrite cached
}
