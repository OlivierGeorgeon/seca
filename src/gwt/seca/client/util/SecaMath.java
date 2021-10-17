package gwt.seca.client.util;

import java.util.ArrayList;

import javax.vecmath.Point3i;
import javax.vecmath.Vector3f;

public class SecaMath {
	
//	/**
//	 * Enumerate the faces of a box. 
//	 * 'Top' is the face in the direction of z increasing. 
//	 * 'Bottom' is the face in the direction of z decreasing.
//	 * 'North' is the face in the direction of y increasing. 
//	 * 'South' is the face in the direction of y decreasing.
//	 * 'East' is the face in the direction of x increasing. 
//	 * 'West' is the face in the direction of x decreasing.
//	 * @author stagiaire
//	 *
//	 */
//	public static enum Face {None, Top, Bottom, North, South, East, West};
	/**
	 * Enumerate the cardinal directions.
	 * 'East' is the direction of x increasing. 
	 * 'West' is the direction of x decreasing.
	 * 'North' is the direction of y increasing. 
	 * 'South' is the direction of y decreasing.
	 * 'Up' is the direction of z increasing. 
	 * 'Down' is the direction of z decreasing.
	 * @author stagiaire
	 *
	 */
	public static enum CardinalDirection {None, East, West, North, South, Up, Down};
	/**
	 * Enumerate the relative directions.
	 * Without any rotation:
	 * 'Right' is 'East', the direction of x increasing. 
	 * 'Left' is 'West', the direction of x decreasing.
	 * 'Forward' is 'North', the direction of y increasing. 
	 * 'Backward' is 'South', the direction of y decreasing.
	 * 'Up' is 'Up', in the direction of z increasing. 
	 * 'Down' is 'Down', the direction of z decreasing.
	 * @author stagiaire
	 *
	 */
	public static enum RelativeDirection {None, Right, Left, Forward, Backward, Up, Down};
	
	public final static int OUT = 0;
	public final static int INTERSECT = 1;
	public final static int IN = 2;
	public final static float INV_SQRT_2 = (float) (1/Math.sqrt(2));
	public final static float INV_SQRT_3 = (float) (1/Math.sqrt(3));
	
	public static boolean isEven(int number) {
		return (Math.round(number/2)*2==number);
	}

	public static Pair<Boolean, Float> intersect(Ray ray, Sphere sphere) {
		return intersect(ray, sphere, true);
	}
	public static Pair<Boolean, Float> intersect(Ray ray, Sphere sphere, boolean discardInside) {
//		//-------------------------------------------------------------------------------------------------
//		// Fonction: détermine le coefficient de collision du premier point d'intersection entre une sphere et un rayon
//		// note    : si le vecteur directeur du rayon n'est pas normalisé le résultat est imprévisible
//		//		         : retourne vrai si t prend cette valeur, faux si l'intersection n'existe pas
//		//-------------------------------------------------------------------------------------------------
//		bool SphereRayIntersect(const Vector3df& sphCenter,
//		double sphRadius,
//		const Vector3df& point0,
//		const Vector3df& rayD,
//		double* t)
//		{
//		// la technique de cette fonction est purement analytique, et non géométrique
//		// le rayon est paramétré par P = Point0 + t * RayD
//		// la sphère est décrite par: norme(P - SCenter)^2 = SRadius^2
//		// la résolution de ce système donne un polynôme du deuxième degré en t
//		// on trouve a = RayD.RayD, b = RayD.dist * 2, c = dist.dist - SRadius^2
//		// on trouve d = b^2 - 4ac
//		// après simplifications (notamment pdtScalaire(RayD, RayD) = 1)
//		// on trouve t1 = RayD.dist - sqrt((RayD.dist)^2 - dist.dist + SRadius^2) et t2 pareil mais avec +
//		 
//		Vector3df dist;
//		double t1;
//		double d;  // discriminant réduit
//		double b;
//		double sqd;
//		 
//		dist = sphCenter - point0;  // inversé par rapport au calcul pour éviter un * -1 plus tard
//		b = rayD * dist;  // le vrai b est -2 * celui-ci
//		d = b * b - dist * dist + sphRadius * sphRadius;
//		if (d < 0.0f)
//		{
//		// pas de solutions => pas d'intersection !
//		return false;
//		}
//		sqd = sqrt(d);
//		// t1 est la plus petite racine (entre t1 = b - sqd et t2 = b + sqd)
//		// c'est le premier point de collision: donc c'est lui qui nous interresse
//		t1 = b - sqd;   // pas -b car b est déjà inversé (opposé)
//		*t = t1;
//		return true;
//		}
		
//		Vector3f dist = new Vector3f();
//		float t;
//		float d;  // discriminant réduit
//		float b;
//		float sqd;
//		
////		dist = sphCenter - point0;  
////		b = rayD * dist;  
////		d = b * b - dist * dist + sphRadius * sphRadius;
//
//		dist.negate(ray.getOrigin());
//		dist.add(sphere.getCenter()); // inversé par rapport au calcul pour éviter un * -1 plus tard
//		b = ray.getDirection().dot(dist); // le vrai b est -2 * celui-ci
//		d = b*b - dist.lengthSquared() + sphere.getRadiusSquared();
//		if (d < 0f) {
//			// pas de solutions => pas d'intersection !
//			return Pair.create(false, 0f);
//		} else {
//			sqd = (float) Math.sqrt(d);
//			// t1 est la plus petite racine (entre t1 = b - sqd et t2 = b + sqd)
//			// c'est le premier point de collision: donc c'est lui qui nous interresse
//			t = b - sqd;   // pas -b car b est déjà inversé (opposé)
//			return Pair.create(true, t);
//		}
		
		//http://www.limsi.fr/Individu/jacquemi/IG-TR-4-5-6/coll-inters-dte-sphere2.html
		Vector3f oc = new Vector3f();
		oc.negate(ray.getOrigin());
		oc.add(sphere.getCenter());
		float d = oc.dot(ray.getDirection());
		float oc2 = oc.lengthSquared();
		float r2 = sphere.getRadiusSquared();
		if( oc2 < r2 && d < 0 ) {
			// sphère derrière l'origine et origine extérieure à la sphère
			return Pair.create(false, 0f);
		}
		float h2 = oc2 - d*d;
		if (h2 > r2) {
			// le rayon ne coupe pas la sphère
			return Pair.create(false, 0f);
		}
		// il y a intersection et on recherche le point d'intersection
		float dp = (float) Math.sqrt(r2 - h2);
		// origine dans la sphère, on retourne le point le plus lointain
		if( oc2 < r2 )
		  return Pair.create(false, d + dp);
		// origine hors de la sphère, on retourne le point le plus proche
		else
		  return Pair.create(false, d - dp);
	}
	public static Pair<Boolean, Float> intersect(Ray ray, Plane plane) {
		float a = ray.getDirection().dot(plane.getNormal());
		float b = ray.getOrigin().dot(plane.getNormal());
		if (Math.abs(a) < 0.001f) {
			//The ray is parallal to the plane
			if (Math.abs(b) < 0.001f) {
				//The ray is in the plane
				return Pair.create(true, 0f);
			} else {
				//The ray does not intersect the plane
				return Pair.create(false, 0f);
			}
		} else {
			//The ray intersects the plane
			float t;
			t = (plane.getConstant()-b)/a;
			return Pair.create(true, t);
		}
	}

	public static Pair<Boolean, Float> intersect(Ray ray, AxisAlignedBox box) {
		//http://www.limsi.fr/Individu/jacquemi/IG-TR-4-5-6/coll-inters-dte-boite1.html
		Vector3f oc = new Vector3f();
		oc.negate(ray.getOrigin());
		oc.add(box.getCenter());
		float Tmin = Float.NEGATIVE_INFINITY;
		float Tmax = Float.POSITIVE_INFINITY;
		Vector3f[] b = new Vector3f[3];
		b[0] = new Vector3f(1, 0, 0);
		b[1] = new Vector3f(0, 1, 0);
		b[2] = new Vector3f(0, 0, 1);
		float[] h = new float[3];
		h[0] = box.getHalfSize().getX();
		h[1] = box.getHalfSize().getY();
		h[2] = box.getHalfSize().getZ();
		
		for(int i=0; i<3; i++) {
			float d1 = oc.dot(b[i]);
			float d1p = ray.getDirection().dot(b[i]);
			if (Math.abs(d1p) < 0.001f) {
				//The ray is parallal to the slice
				if (Math.abs(d1) < h[i]) {
					//The ray is in the slice
				} else {
					//The ray does not intersect the slice
					return Pair.create(false, 0f);
				}
			} else {
				//The ray intersects the slice
				// calcul des paramètres des intersections avec les côtés de la plaque
			    float t1 = Math.min((d1+h[i])/d1p, (d1-h[i])/d1p);
			    float t2 = Math.max((d1+h[i])/d1p, (d1-h[i])/d1p);
			    // mise à jour des minimaux et maximaux
			    if (Tmin < t1) Tmin = t1;
			    if (Tmax > t2) Tmax = t2;
			    if (Tmin > Tmax) {
			    	// cas d'intersection vide (cf. Cyrus Beck)
			    	return Pair.create(false, 0f);
			    }
			    if (Tmax < 0) {
			    	// plaque derrière l'origine du rayon
			    	return Pair.create(false, 0f);
			    }
			}
		}
		if( Tmin > 0 ) {
			// intersection existe et la valeur minimale est devant l'origine
			return Pair.create(true, Tmin);
		} else {
			// intersection existe et la valeur minimale est derrière l'origine
			return Pair.create(true, Tmax);
		}
	}
	
//	public static Pair<Boolean, Float> intersect(Ray ray, PlaneBoundedVolume volume) {
//	// TODO Auto-generated method stub
//	return null;
//}

	public static boolean intersect(Sphere sphere, Plane plane) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public static boolean intersect(Sphere sphere, AxisAlignedBox box) {
		// TODO Auto-generated method stub
		return false;
	}

	public static boolean intersect(Plane plane, AxisAlignedBox box) {
		// TODO Auto-generated method stub
		return false;
	}
	
	/**
	 * Test if a sphere in within a list of planes
	 * @param planes
	 * @param sphere
	 * @return
	 */
	public static int contain(Plane[] planes, Sphere sphere) {
		//Various distances
		float fDistance;

		//Calculate the distances to each of the planes
		for(int i=0; i<planes.length; i++) {

			//Find the distance to this plane
			fDistance = planes[i].getDistance(sphere.getCenter()) + planes[i].getConstant();

			//If this distance is < -sphere.radius, we are outside
			if(fDistance < -sphere.getRadius())
				return(OUT);

			//Else if the distance is between +- radius, then we intersect
			if(Math.abs(fDistance) < sphere.getRadius())
				return(INTERSECT);
		}

		// otherwise we are fully in view
		return IN;
	}

	public static void rotateZ(Vector3f vector, float dTheta, float dPhi) {
		//Passage en coord sphériques
		float r = (float) Math.sqrt(vector.x*vector.x+vector.y*vector.y+vector.z*vector.z);
		float theta = (float) Math.atan2(vector.y, vector.x);
		float phi = (float) Math.atan2(Math.sqrt(vector.x*vector.x+vector.y*vector.y), vector.z);
		phi += Math.toRadians(dPhi);
		theta += Math.toRadians(dTheta);
		if (phi<0.001f) {
			phi = 0.001f;
		} else if (phi > Math.PI) {
			phi = (float) (Math.PI - 0.001f);
		}
		vector.x = (float) (r*Math.sin(phi)*Math.cos(theta));
		vector.y = (float) (r*Math.sin(phi)*Math.sin(theta));
		vector.z = (float) (r*Math.cos(phi));
	}
	
	public static Point3i getAdjacent(Point3i position, CardinalDirection direction) {
		Point3i adj = new Point3i(position);
		switch(direction) {
		case Up:
			adj.z++;
			break;
		case Down:
			adj.z--;
			break;
		case North:
			adj.y++;
			break;
		case South:
			adj.y--;
			break;
		case East:
			adj.x++;
			break;
		case West:
			adj.x--;
			break;
		}
		return adj;
	}
	public static AxisAlignedBox getTranslatedAAB(AxisAlignedBox box, Vector3f trans) {
		AxisAlignedBox aab = new AxisAlignedBox(box);
		aab.translate(trans);
		return aab;
	}
	public static AxisAlignedBox getTranslatedAAB(AxisAlignedBox box, Point3i trans) {
		return getTranslatedAAB(box, new Vector3f(trans.x, trans.y, trans.z));
	}
	public static ArrayList<Pair<Point3i, CardinalDirection>> getCellPath(Vector3f origin, Vector3f destination) {
		ArrayList<Pair<Point3i, CardinalDirection>> cellPath = new ArrayList<Pair<Point3i, CardinalDirection>>();
		//TODO
		float dx = Math.abs(destination.x - origin.x);
		float dy = Math.abs(destination.y - origin.y);
		float dz = Math.abs(destination.z - origin.z);
	    int i = Math.round(origin.x);
	    int j = Math.round(origin.y);
	    int k = Math.round(origin.z);
	    float dt_dx = 1/dx;
	    float dt_dy = 1/dy;
	    float dt_dz = 1/dz;
	    float t = 0;
	    int n = 1;
	    int di, dj, dk;
	    float t_nextAlongY, t_nextAlongX, t_nextAlongZ;
	    CardinalDirection directionAlongX, directionAlongY, directionAlongZ, lastDirection = CardinalDirection.None;

	    if (dx == 0) {
	        di = 0;
	        t_nextAlongX = dt_dx; // infinity
	        directionAlongX = CardinalDirection.None;
	    } else if (destination.x > origin.x) {
	        di = 1;
	        n += Math.round(destination.x) - i;
	        t_nextAlongX = ((origin.x + .5f) - i) * dt_dx;
	        directionAlongX = CardinalDirection.West;
	    } else {
	        di = -1;
	        n += i - Math.round(destination.x);
	        t_nextAlongX = (i - (origin.x - .5f)) * dt_dx;
	        directionAlongX = CardinalDirection.East;
	    }
	    if (dy == 0) {
	        dj = 0;
	        t_nextAlongY = dt_dy; // infinity
	        directionAlongY = CardinalDirection.None;
	    } else if (destination.y > origin.y) {
	        dj = 1;
	        n += Math.round(destination.y) - j;
	        t_nextAlongY = ((origin.y + .5f) - j) * dt_dy;
	        directionAlongY = CardinalDirection.South;
	    } else {
	        dj = -1;
	        n += j - Math.round(destination.y);
	        t_nextAlongY = (j - (origin.y - .5f)) * dt_dy;
	        directionAlongY = CardinalDirection.North;
	    }
	    if (dz == 0) {
	        dk = 0;
	        t_nextAlongZ = dt_dz; // infinity
	        directionAlongZ = CardinalDirection.None;
	    } else if (destination.z > origin.z) {
	        dk = 1;
	        n += Math.round(destination.z) - k;
	        t_nextAlongZ = ((origin.z + .5f) - k) * dt_dz;
	        directionAlongZ = CardinalDirection.Down;
	    } else {
	        dk = -1;
	        n += k - Math.round(destination.z);
	        t_nextAlongZ = (k - (origin.z - .5f)) * dt_dz;
	        directionAlongZ = CardinalDirection.Up;
	    }

	    for (; n > 0; --n) {
	        //visit(i, j, k);
	    	cellPath.add(Pair.create(new Point3i(i, j, k), lastDirection));
	        if (t_nextAlongX <= t_nextAlongY && t_nextAlongX <= t_nextAlongZ) {
	        	i += di;
	            t = t_nextAlongX;
	            t_nextAlongX += dt_dx;
	            lastDirection = directionAlongX;
	        } else if (t_nextAlongY <= t_nextAlongX && t_nextAlongY <= t_nextAlongZ)
	        {
	        	j += dj;
	            t = t_nextAlongY;
	            t_nextAlongY += dt_dy;
	            lastDirection = directionAlongY;
	        } else {
	        	k += dk;
	            t = t_nextAlongZ;
	            t_nextAlongZ += dt_dz;
	            lastDirection = directionAlongZ;
	        }
	    }
		return cellPath;
	}

//	static std::pair<bool, Real> intersects(const Ray& ray, const Plane& plane);
//	00435 
//	00437         static std::pair<bool, Real> intersects(const Ray& ray, const Sphere& sphere, 
//	00438             bool discardInside = true);
//	00439         
//	00441         static std::pair<bool, Real> intersects(const Ray& ray, const AxisAlignedBox& box);
//	00442 
//	00465         static bool intersects(const Ray& ray, const AxisAlignedBox& box,
//	00466             Real* d1, Real* d2);
//	00467 
//	00492         static std::pair<bool, Real> intersects(const Ray& ray, const Vector3& a,
//	00493             const Vector3& b, const Vector3& c, const Vector3& normal,
//	00494             bool positiveSide = true, bool negativeSide = true);
//	00495 
//	00516         static std::pair<bool, Real> intersects(const Ray& ray, const Vector3& a,
//	00517             const Vector3& b, const Vector3& c,
//	00518             bool positiveSide = true, bool negativeSide = true);
//	00519 
//	00521         
//	00525 
//	00531         static std::pair<bool, Real> intersects(
//	00532             const Ray& ray, const vector<Plane>::type& planeList, 
//	00533             bool normalIsOutside);
//	00539         static std::pair<bool, Real> intersects(
//	00540             const Ray& ray, const list<Plane>::type& planeList, 
//	00541             bool normalIsOutside);
//	00542 
//	00546         
}
