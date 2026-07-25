package com.samialsohan.linkly.service;

public sealed interface CacheLookup {
    record Hit(String longURL) implements CacheLookup{}
    record  NegativeHit() implements CacheLookup{}
    record Miss() implements CacheLookup{}
}
