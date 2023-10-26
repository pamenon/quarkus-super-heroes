package io.quarkus.sample.superheroes.location.mapping

import io.quarkus.sample.superheroes.location.Location
import io.quarkus.sample.superheroes.location.LocationType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class LocationMapperTests {
	companion object {
		private const val DEFAULT_ID = 1L
		private const val DEFAULT_NAME = "Gotham City"
		private const val DEFAULT_DESCRIPTION = "Where Batman lives"
		private const val DEFAULT_PICTURE = "gotham_city.png"
		private val DEFAULT_TYPE = LocationType.CITY

		private fun createDefaultLocation() : Location {
			val location = Location()
			location.id = DEFAULT_ID
			location.name = DEFAULT_NAME
			location.description = DEFAULT_DESCRIPTION
			location.picture = DEFAULT_PICTURE
			location.type = DEFAULT_TYPE

			return location
		}
	}

	@Test
	fun `mapper works correctly for null`() {
		assertThat(LocationMapper.toGrpcLocationMaybeNull(null))
			.isNull()
	}

	@Test
	fun `mapper works correctly for non-null`() {
		assertThat(LocationMapper.toGrpcLocation(createDefaultLocation()))
			.isNotNull
			.extracting(
				"name",
				"description",
				"picture",
				"type"
			)
			.containsExactly(
				DEFAULT_NAME,
				DEFAULT_DESCRIPTION,
				DEFAULT_PICTURE,
				io.quarkus.sample.superheroes.location.grpc.LocationType.CITY
			)
	}
}