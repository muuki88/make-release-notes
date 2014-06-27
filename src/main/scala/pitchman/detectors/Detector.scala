package pitchman.detectors

import pitchman.model.Commit

/**
 *
 */
trait Detector[T] extends (Commit => Option[T])