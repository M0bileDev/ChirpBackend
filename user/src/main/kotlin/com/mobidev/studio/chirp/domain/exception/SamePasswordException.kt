package com.mobidev.studio.chirp.domain.exception

class SamePasswordException : RuntimeException("The new password cannot be the same as old one")