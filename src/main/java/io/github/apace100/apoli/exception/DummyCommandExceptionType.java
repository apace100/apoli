package io.github.apace100.apoli.exception;

import com.mojang.brigadier.exceptions.CommandExceptionType;

public final class DummyCommandExceptionType implements CommandExceptionType {

	public static final DummyCommandExceptionType INSTANCE = new DummyCommandExceptionType();

	private DummyCommandExceptionType() {

	}

}
